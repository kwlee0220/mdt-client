package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.DataUtils;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.func.FOption;
import utils.http.RESTfulRemoteException;
import utils.stream.FStream;

import mdt.client.operation.HttpOperationClient;
import mdt.model.AASUtils;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;
import mdt.model.sm.variable.Variable;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask implements MDTTask, CancellableWork, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final javax.xml.datatype.Duration INFINITE = AASUtils.DATATYPE_FACTORY.newDuration("P7D");

	public static final String OPTION_OPERATION = "operation";
	public static final String OPTION_POLL_INTERVAL = "poll";
	public static final String OPTION_TIMEOUT = "timeout";	// optional
	public static final String OPTION_LOG_LEVEL = "loglevel";
	public static final String OPTION_UPDATE_OPVARS = "updateOperationVariables";
	public static final String OPTION_SHOW_RESULT = "showResult";
	
	private final TaskDescriptor m_descriptor;
	private Logger m_logger;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp = null;
	@GuardedBy("m_guard") private OperationResult m_opResult;
	
	public AASOperationTask(TaskDescriptor descriptor) {
		Preconditions.checkArgument(descriptor != null, "TaskDescriptor is null");
		
		m_descriptor = descriptor;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		Instant started = Instant.now();
		
		TaskDescriptor descriptor = getTaskDescriptor();
		MDTElementReference opRef
					= descriptor.findOptionValue(OPTION_OPERATION)
								.map(ElementReferences::parseExpr)
								.getOrThrow(() -> new IllegalArgumentException("Option[operation] is not provided"));
		Operation op;
		try {
			opRef.activate(manager);
			SubmodelElement sme = opRef.read();
			Preconditions.checkState(sme instanceof Operation, "Target SubmodelElement is not an Operation: " + sme);
			op = (Operation)sme;
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to read Operation: ref=" + opRef, e);
		}
		
		// TaskDescriptor에 기술된 input/output variable을 이용해서 in/inout OperationVariable을 설정한다.
		setInputVariables(manager, op);
		
		OperationResult result = invokeOperation(opRef, op);
		m_guard.run(() -> m_opResult = result);
		
		if ( !result.getSuccess() ) {
			String fullMsg = FStream.from(m_opResult.getMessages())
									.map(msg -> msg.getText())
									.join(System.lineSeparator());
			throw new TaskException(new RESTfulRemoteException("Operation failed: msg=" + fullMsg));
		}
		
		// ASOperation 수행 결과를 output task variable들에 반영한다.
		updateOutputVariables(m_opResult);
		
		boolean updateOperationVariables = descriptor.findOptionValue(OPTION_UPDATE_OPVARS)
														.map(DataUtils::asBoolean)
														.getOrElse(false);
		if ( updateOperationVariables ) {
			try {
				op.setInoutputVariables(result.getInoutputArguments());
				op.setOutputVariables(result.getOutputArguments());
				opRef.write(op);
			}
			catch ( IOException e ) {
				getLogger().warn("Failed to update OperationVariables: ref=" + opRef, e);
			}
		}

		// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
		TaskUtils.updateLastExecutionTime(manager, m_descriptor, started, getLogger());
		
		boolean showResult = descriptor.findOptionValue(OPTION_SHOW_RESULT)
										.map(DataUtils::asBoolean)
										.getOrElse(false);
		if ( showResult ) {
			for ( OperationVariable opVar: result.getOutputArguments() ) {
				ElementValue ev = ElementValues.getValue(opVar.getValue());
				System.out.printf("[output] %s: %s%n", opVar.getValue().getIdShort(), ev.toDisplayString());
			}
			for ( OperationVariable opVar: result.getInoutputArguments() ) {
				ElementValue ev = ElementValues.getValue(opVar.getValue());
				System.out.printf("[inoutput] %s: %s%n", opVar.getValue().getIdShort(), ev.toDisplayString());
			}
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public boolean cancelWork() {
		return true;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	
	private void setInputVariables(MDTInstanceManager manager, Operation op) throws TaskException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		// TaskDescriptor에	설정된 input/output variable들을 활성화한다.
		FStream.from(descriptor.getInputVariables())
				.concatWith(FStream.from(descriptor.getOutputVariables()))
				.castSafely(ReferenceVariable.class)
				.forEach(refVar -> refVar.activate(manager));
		
		
		// 명령어 인자로 parameter 값들이 설정된 경우에는, 설정된 값으로 OperationVariable을 채운다.
		try {
			for ( OperationVariable opv: op.getInputVariables() ) {
				String opvId = opv.getValue().getIdShort();
				Variable taskVar = descriptor.getInputVariables().getOfKey(opvId);
				if ( taskVar != null ) {
					SubmodelElement opvSme = opv.getValue();
					if ( taskVar instanceof ValueVariable valVar ) {
						ElementValues.update(opvSme, valVar.readValue());
					}
					else {
						SubmodelElement varSme = taskVar.read();
						if ( SubmodelUtils.isParameterValue(varSme) && opv.getValue() instanceof Property ) {
							varSme = SubmodelUtils.traverse(varSme, "ParameterValue");
						}
						ElementValues.update(opvSme, ElementValues.getValue(varSme));
					}
				}
			}
			for ( OperationVariable opv: op.getInoutputVariables() ) {
				String opvId = opv.getValue().getIdShort();
				Variable taskVar = descriptor.getInputVariables().getOfKey(opvId);
				if ( taskVar != null ) {
					SubmodelElement varSme = taskVar.read();
					if ( SubmodelUtils.isParameterValue(varSme) && opv.getValue() instanceof Property ) {
						varSme = SubmodelUtils.traverse(varSme, "ParameterValue");
					}
					ElementValues.update(opv.getValue(), ElementValues.getValue(varSme));
				}
			}
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to update OperationVariable", e);
		}
	}

	private OperationResult invokeOperation(MDTElementReference opRef, Operation op)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		SubmodelService svc = opRef.getSubmodelService();
		String opIdShortPath = opRef.getIdShortPathString();

		Duration pollInterval = descriptor.findOptionValue(OPTION_POLL_INTERVAL)
											.map(UnitUtils::parseSecondDuration)
											.getOrElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = descriptor.findOptionValue(OPTION_TIMEOUT)
									.map(UnitUtils::parseSecondDuration)
									.getOrNull();
		
		OperationResult result = null;
//		if ( !sync ) {
			getLogger().info("invoking AASOperation asynchronously: op={}, timeout={}, pollInterval={}",
							opRef, timeout, pollInterval);	
			try {
				result = svc.runOperation(opIdShortPath, op.getInputVariables(),
											op.getInoutputVariables(), timeout, pollInterval);
			}
			catch ( ExecutionException e ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				if ( cause instanceof RESTfulRemoteException rmt ) {
					if ( rmt.getCause() != null ) {
						cause = Throwables.unwrapThrowable(rmt.getCause());
					}
				}
				Throwables.throwIfInstanceOf(cause, TaskException.class);
				throw new TaskException(cause);
			}
//		}
//		else {
//			getLogger().info("invoking AASOperation synchronously: op={}, timeout={}", opRef, timeout);	
//			javax.xml.datatype.Duration jtimeout
//								= FOption.mapOrElse(timeout,
//													to -> AASUtils.DATATYPE_FACTORY.newDuration(to.toMillis()), INFINITE);
//			result = svc.invokeOperationSync(opIdShortPath, op.getInputVariables(),
//														op.getInoutputVariables(), jtimeout);
//		}
		
		return result;
	}
	
	private void updateOutputVariables(OperationResult opResult) {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		FStream.from(opResult.getInoutputArguments())
				.forEachOrIgnore(opvar -> {
					SubmodelElement resultSme = opvar.getValue();
					Variable taskVar = descriptor.getOutputVariables().getOfKey(resultSme.getIdShort());
					if ( taskVar != null ) {
						taskVar.update(resultSme);
						if ( getLogger().isInfoEnabled() ) {
							getLogger().info("Updated: output variable[{}]: {}",
											taskVar.getName(), ElementValues.getValue(resultSme));
						}
					}
				});
		FStream.from(opResult.getOutputArguments())
				.forEachOrIgnore(opvar -> {
					SubmodelElement resultSme = opvar.getValue();
					Variable taskVar = descriptor.getOutputVariables().getOfKey(resultSme.getIdShort());
					if ( taskVar != null ) {
						taskVar.update(resultSme);
						if ( getLogger().isInfoEnabled() ) {
							getLogger().info("Updated: output variable[{}]: {}",
											taskVar.getName(), ElementValues.getValue(resultSme));
						}
					}
				});
	}
	private Duration parseDuration(Object seconds) {
		try {
			long millSeconds = Math.round(DataUtils.asDouble(seconds) * 1000);
			return Duration.ofMillis(millSeconds);
		}
		catch ( NumberFormatException e ) {
			return UnitUtils.parseDuration("" + seconds);
		}
	}
}
