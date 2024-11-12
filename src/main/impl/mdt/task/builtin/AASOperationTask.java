package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.KeyedValueList;
import utils.func.FOption;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.SubmodelService;
import mdt.model.sm.MDTSubmodelElementReference;
import mdt.task.MultiParameterTask;
import mdt.task.Parameter;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask extends MultiParameterTask {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final javax.xml.datatype.Duration INFINITE = AASUtils.DATATYPE_FACTORY.newDuration("P1D");

	private final MDTSubmodelElementReference m_operationReference;
	private final boolean m_async;
	private final Duration m_pollInterval;
	private final Duration m_timeout;
	
	public AASOperationTask(MDTSubmodelElementReference opRef, boolean async, Duration pollInterval,
							@Nullable Duration timeout,
							KeyedValueList<String,Parameter> parameters,
							Set<String> outputParameterNames) {
		super(parameters, outputParameterNames);
		
		this.m_operationReference = opRef;
		this.m_async = async;
		this.m_pollInterval = pollInterval;
		this.m_timeout = timeout;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			this.m_operationReference.activate(manager);
			
			Operation op = m_operationReference.getAsOperation();
			SubmodelService svc = m_operationReference.getSubmodelService();
			String opIdShortPath = m_operationReference.getElementIdShortPath();
			
			OperationResult result = null;
			if ( this.m_async ) {
				if ( s_logger.isInfoEnabled() ) {
					s_logger.info("invoking AASOperation asynchronously: op={}, timeout={}, pollInterval={}",
									this.m_operationReference, m_timeout, this.m_pollInterval);	
				}
				try {
					result = svc.runOperation(opIdShortPath, op.getInputVariables(), op.getInoutputVariables(),
												m_timeout, this.m_pollInterval);
				}
				catch ( ExecutionException e ) {
					Throwable cause = e.getCause();
					throw new TaskException(cause);
				}
			}
			else {
				if ( s_logger.isInfoEnabled() ) {
					s_logger.info("invoking AASOperation synchronously: op={}, timeout={}",
									this.m_operationReference, m_timeout);	
				}
				javax.xml.datatype.Duration jtimeout = FOption.mapOrElse(m_timeout,
																	to -> AASUtils.DATATYPE_FACTORY.newDuration(to.toMillis()),
																	INFINITE);
				result = svc.invokeOperationSync(opIdShortPath, op.getInputVariables(), op.getInoutputVariables(),
												jtimeout);
			}
			
			op.setInoutputVariables(result.getInoutputArguments());
			op.setOutputVariables(result.getOutputArguments());
			m_operationReference.write(op);
		}
		catch ( IOException e ) {
			throw new TaskException(e);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
	
//	public static TaskDescriptor getTemplateDescriptor() {
//		TaskDescriptor tmplt = new TaskDescriptor();
//		tmplt.setId("aas");
//		tmplt.setName("AAS Operation 구동 태스크");
//		tmplt.setType(AASOperationTask.class.getName());
//		tmplt.setDescription("AAS 모델 내 Operation을 구동하는 태스크.");
//		
//		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
//		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
//		tmplt.getOptions().add(new OptionDescriptor("operation", true,
//													"실행시킬 Operation SubmodelElement reference", null));
//		tmplt.getOptions().add(new OptionDescriptor("async", false, "비동기 태스크 수행 여부. (default: false)", null));
//		tmplt.getOptions().add(new OptionDescriptor("timeout", false, "태스크 수행 제한 시간", null));
//		
//		return tmplt;
//	}
}
