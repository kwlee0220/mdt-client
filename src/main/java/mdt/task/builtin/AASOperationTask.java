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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.LoggerSettable;
import utils.Throwables;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.func.FOption;
import utils.http.RESTfulRemoteException;
import utils.stream.FStream;

import mdt.client.operation.HttpOperationClient;
import mdt.model.AASUtils;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.Variable;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.BooleanOption;
import mdt.workflow.model.DurationOption;
import mdt.workflow.model.MDTElementRefOption;
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
	public static final String OPTION_SYNC = "sync";
	public static final String OPTION_UPDATE_OPVARS = "updateOperationVariables";
	
	private final TaskDescriptor m_descriptor;
	private Logger m_logger;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp = null;
	
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
					= descriptor.findOption(OPTION_OPERATION, MDTElementRefOption.class)
								.map(MDTElementRefOption::getValue)
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
		
		// TaskPort를 이용해서 OperationVariable을 업데이트한다.
		setInputVariables(manager, op);
		
		OperationResult result = invokeOperation(opRef, op);
		updateOutputVariables(result);
		
		boolean updateOperationVariables = descriptor.findOption(OPTION_UPDATE_OPVARS, BooleanOption.class)
														.map(BooleanOption::getValue)
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
				.forEach(rport -> rport.activate(manager));
		
		
		// 명령어 인자로 parameter 값들이 설정된 경우에는, 설정된 값으로 OperationVariable을 채운다.
		try {
			FStream.from(op.getInputVariables())
					.tagKey(opv -> opv.getValue().getIdShort())
					.innerJoin(descriptor.getInputVariables().fstream())
					.forEachOrThrow(match -> {
						OperationVariable inVar = match.value()._1;
						Variable taskVar = match.value()._2;
						ElementValues.update(inVar.getValue(), taskVar.readValue());
					});
			
			FStream.from(op.getOutputVariables())
					.tagKey(opv -> opv.getValue().getIdShort())
					.innerJoin(descriptor.getOutputVariables().fstream())
					.forEachOrThrow(match -> {
						OperationVariable inVar = match.value()._1;
						Variable taskVar = match.value()._2;
						ElementValues.update(inVar.getValue(), taskVar.readValue());
					});
			
			FStream.from(op.getInoutputVariables())
					.tagKey(opv -> opv.getValue().getIdShort())
					.innerJoin(descriptor.getInputVariables().fstream())
					.forEachOrThrow(match -> {
						OperationVariable inoutVar = match.value()._1;
						Variable taskVar = match.value()._2;
						ElementValues.update(inoutVar.getValue(), taskVar.readValue());
					});
			FStream.from(op.getInoutputVariables())
					.tagKey(opv -> opv.getValue().getIdShort())
					.innerJoin(descriptor.getOutputVariables().fstream())
					.forEachOrThrow(match -> {
						OperationVariable inoutVar = match.value()._1;
						Variable taskVar = match.value()._2;
						ElementValues.update(inoutVar.getValue(), taskVar.readValue());
					});
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

		boolean sync = descriptor.findOption(OPTION_SYNC, BooleanOption.class)
									.map(BooleanOption::getValue)
									.getOrElse(false);
		Duration pollInterval = descriptor.findOption(OPTION_POLL_INTERVAL, DurationOption.class)
											.map(DurationOption::getValue)
											.getOrElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = descriptor.findOption(OPTION_TIMEOUT, DurationOption.class)
									.map(DurationOption::getValue)
									.getOrNull();
		
		OperationResult result = null;
		if ( !sync ) {
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("invoking AASOperation asynchronously: op={}, timeout={}, pollInterval={}",
								opRef, timeout, pollInterval);	
			}
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
		}
		else {
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("invoking AASOperation synchronously: op={}, timeout={}", opRef, timeout);	
			}
			javax.xml.datatype.Duration jtimeout
								= FOption.mapOrElse(timeout,
													to -> AASUtils.DATATYPE_FACTORY.newDuration(to.toMillis()), INFINITE);
			result = svc.invokeOperationSync(opIdShortPath, op.getInputVariables(),
														op.getInoutputVariables(), jtimeout);
		}
		
		return result;
	}
	
	private void updateOutputVariables(OperationResult opResult) {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		FStream.from(opResult.getInoutputArguments())
				.forEachOrIgnore(opvar -> {
					SubmodelElement resultSme = opvar.getValue();
					Variable port = descriptor.getOutputVariables().getOfKey(resultSme.getIdShort());
					if ( port != null ) {
						port.update(resultSme);
						if ( getLogger().isInfoEnabled() ) {
							getLogger().info("Updated: output variable[{}]: {}", port.getName(), resultSme);
						}
					}
				});
		FStream.from(opResult.getOutputArguments())
				.forEachOrIgnore(opvar -> {
					SubmodelElement resultSme = opvar.getValue();
					Variable port = descriptor.getOutputVariables().getOfKey(resultSme.getIdShort());
					if ( port != null ) {
						port.update(resultSme);
						if ( getLogger().isInfoEnabled() ) {
							getLogger().info("Updated: output variable[{}]: {}", port.getName(), resultSme);
						}
					}
				});
	}
}
