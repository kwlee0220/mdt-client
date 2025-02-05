package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.func.FOption;
import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.AASUtils;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;
import mdt.task.MDTTask;
import mdt.task.Parameter;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask extends AbstractThreadedExecution<Void> implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final javax.xml.datatype.Duration INFINITE = AASUtils.DATATYPE_FACTORY.newDuration("P7D");

	private MDTInstanceManager m_manager;
	private final MDTElementReference m_operationReference;
	private final List<Parameter> m_inputParameters;
	private final List<Parameter> m_outputParameters;
	private final boolean m_async;
	private final Duration m_pollInterval;
	private final Duration m_timeout;
	private final boolean m_updateOperation;
	private final boolean m_showOutputVariables;
	
	private AASOperationTask(Builder builder) {
		Preconditions.checkNotNull(builder.m_operationReference);
		
		m_operationReference = builder.m_operationReference;
		m_inputParameters = builder.m_inputParameters;
		m_outputParameters = builder.m_outputParameters;
		m_async = builder.m_async;
		m_pollInterval = FOption.getOrElse(builder.m_pollInterval, Duration.ofSeconds(3));
		m_timeout = builder.m_timeout;
		m_updateOperation = builder.m_updateOperation;
		m_showOutputVariables = builder.m_showOutputVariables;
		
		setLogger(s_logger);
	}

	@Override
	public void run(MDTInstanceManager manager)
			throws TimeoutException, InterruptedException, CancellationException, TaskException {
		m_manager = manager;
		
		try {
			run();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
			throw new TaskException(cause);
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}
	
	private void updateOperationVariable(OperationVariable opv, Parameter valueParam) throws IOException {
		SubmodelElementValue smec = ElementValues.getValue(valueParam.getReference().read());
		ElementValues.update(opv.getValue(), smec);
	}

	@Override
	protected Void executeWork() throws InterruptedException, CancellationException,
													TimeoutException, Exception {
		try {
			this.m_operationReference.activate(m_manager);
			FStream.from(m_inputParameters).forEach(param -> param.activate(m_manager));
			FStream.from(m_outputParameters).forEach(param -> param.activate(m_manager));
			
			Operation op = m_operationReference.getAsOperation();
			SubmodelService svc = m_operationReference.getSubmodelService();
			String opIdShortPath = m_operationReference.getElementPath();
			
			// 명령어 인자로 parameter 값들이 설정된 경우에는, 설정된 값으로 OperationVariable을 채운다.
			List<OperationVariable> inVars = op.getInputVariables();
			List<OperationVariable> outVars = op.getOutputVariables();
			List<OperationVariable> inoutVars = op.getInoutputVariables();
			FStream.from(m_inputParameters)
					.outerJoin(FStream.from(m_outputParameters), Parameter::getName, Parameter::getName)
					.forEachOrThrow(match -> {
						Preconditions.checkState(match._1.size() <= 1);
						Preconditions.checkState(match._2.size() <= 1);
						
						if ( match._1.size() > 0 && match._2.size() > 0 ) {	// inoutput
							Parameter inoutParam = match._1.get(0);
							String name = inoutParam.getName();
							
							Funcs.findFirst(inoutVars, opv -> name.equals(opv.getValue().getIdShort()))
									.ifPresentOrThrow(opv -> updateOperationVariable(opv, inoutParam));
						}
						else if ( match._2.size() == 0 ) {	// input
							Parameter inParam = match._1.get(0);
							String name = inParam.getName();
							
							Funcs.findFirst(inVars, opv -> name.equals(opv.getValue().getIdShort()))
									.ifPresentOrThrow(opv -> updateOperationVariable(opv, inParam));
						}
						else {	// output
							Parameter outParam = match._1.get(0);
							String name = outParam.getName();
							
							Funcs.findFirst(outVars, opv -> name.equals(opv.getValue().getIdShort()))
									.ifPresentOrThrow(opv -> updateOperationVariable(opv, outParam));
						}
					});
			
			OperationResult result = null;
			if ( this.m_async ) {
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("invoking AASOperation asynchronously: op={}, timeout={}, pollInterval={}",
									this.m_operationReference, m_timeout, this.m_pollInterval);	
				}
				try {
					result = svc.runOperation(opIdShortPath, op.getInputVariables(), op.getInoutputVariables(),
												m_timeout, this.m_pollInterval);
				}
				catch ( ExecutionException e ) {
					Throwable cause = Throwables.unwrapThrowable(e);
					Throwables.throwIfInstanceOf(cause, TaskException.class);
					throw new ExecutionException(cause);
				}
			}
			else {
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("invoking AASOperation synchronously: op={}, timeout={}",
									this.m_operationReference, m_timeout);	
				}
				javax.xml.datatype.Duration jtimeout
									= FOption.mapOrElse(m_timeout,
														to -> AASUtils.DATATYPE_FACTORY.newDuration(to.toMillis()),
														INFINITE);
				result = svc.invokeOperationSync(opIdShortPath, op.getInputVariables(), op.getInoutputVariables(),
												jtimeout);
			}
			
			// 연산 수행 결과 output variable의 값을 해당 parameter에 반영한다.
			FStream.from(result.getInoutputArguments())
					.concatWith(FStream.from(result.getOutputArguments()))
					.innerJoin(FStream.from(m_outputParameters), opv -> opv.getValue().getIdShort(), Parameter::getName)
					.forEachOrThrow(match -> {
						OperationVariable resultOpv = match._1;
						Parameter outParam = match._2;
						
						outParam.getReference().write(resultOpv.getValue());
					});
			
			if ( m_updateOperation ) {
				op.setInoutputVariables(result.getInoutputArguments());
				op.setOutputVariables(result.getOutputArguments());
				m_operationReference.write(op);
			}
			
			if ( m_showOutputVariables ) {
				for ( OperationVariable opv: result.getInoutputArguments() ) {
					String str = ElementValues.toRawString(opv.getValue());
					System.out.printf("[inoutput] %s: %s%n", opv.getValue().getIdShort(), str);
				}
				for ( OperationVariable opv: result.getOutputArguments() ) {
					String str = ElementValues.toRawString(opv.getValue());
					System.out.printf("[output  ] %s: %s%n", opv.getValue().getIdShort(), str);
				}
			}
			
			return null;
		}
		catch ( IOException e ) {
			throw new TaskException(e);
		}
	}

	@Override
	public boolean cancel() {
		return true;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private MDTElementReference m_operationReference;
		private KeyedValueList<String,Parameter> m_inputParameters = KeyedValueList.newInstance(Parameter::getName);
		private KeyedValueList<String,Parameter> m_outputParameters = KeyedValueList.newInstance(Parameter::getName);
		private boolean m_async = true;
		private Duration m_pollInterval = null;
		private Duration m_timeout = null;
		private boolean m_updateOperation = false;
		private boolean m_showOutputVariables = false;
		
		public AASOperationTask build() {
			return new AASOperationTask(this);
		}
		
		public Builder operationReference(MDTElementReference ref) {
			m_operationReference = ref;
			return this;
		}
		
		public Builder addInputParameter(Parameter param) {
			m_inputParameters.add(param);
			return this;
		}
		
		public Builder addOutputParameter(Parameter param) {
			m_outputParameters.add(param);
			return this;
		}
		
		public Builder sync(boolean flag) {
			m_async = !flag;
			return this;
		}
		
		public Builder pollInterval(Duration interval) {
			m_pollInterval = interval;
			return this;
		}
		
		public Builder timeout(Duration to) {
			m_timeout = to;
			return this;
		}
		
		public Builder updateOperation(boolean flag) {
			m_updateOperation = flag;
			return this;
		}
		
		public Builder showOutputVariables(boolean flag) {
			m_showOutputVariables = flag;
			return this;
		}
	}
}
