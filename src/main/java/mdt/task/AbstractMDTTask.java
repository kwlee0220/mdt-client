package mdt.task;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.func.Optionals;
import utils.stream.KeyValueFStream;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerAware;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.SubmodelUtils.OperationSubmodelDescriptor;
import mdt.model.sm.SubmodelUtils.SubmodelArgumentDescriptor;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.value.ElementValues;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractMDTTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractMDTTask.class);
	
	private final TaskDescriptor m_descriptor;
	private MDTInstanceManager m_manager;
	private Logger m_logger;
	
	abstract protected Map<String,SubmodelElement> invoke(MDTInstanceManager manager,
															Map<String,SubmodelElement> inputs,
															Map<String,SubmodelElement> outputs)
			throws TimeoutException, InterruptedException, CancellationException, TaskException;

	protected AbstractMDTTask(TaskDescriptor descriptor) {
		Preconditions.checkArgument(descriptor != null, "TaskDescriptor is null");
		m_descriptor = descriptor;
	}
	
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}
	
	public MDTInstanceManager getInstanceManager() {
		return m_manager;
	}

	@Override
	public Map<String,SubmodelElement> run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		Instant started = Instant.now();
		m_manager = manager;
		
		try {
			DefaultSubmodelReference smRef = (DefaultSubmodelReference)m_descriptor.getSubmodelRef();
			Preconditions.checkState(smRef != null,
									"SubmodelReference is not specified in TaskDescriptor: %s", m_descriptor.getId());
			smRef.activate(manager);
			OperationSubmodelDescriptor opSmDesc = SubmodelUtils.loadOperationSubmodelDescriptor(smRef.get().getSubmodel());
			
			LinkedHashMap<String,SubmodelElement> inputArguments = Maps.newLinkedHashMap();
			for ( Map.Entry<String, SubmodelArgumentDescriptor> ent: opSmDesc.getInputs().entrySet() ) {
				String argId = ent.getKey();
				SubmodelElement arg = ent.getValue().getSubmodelElement();
				ArgumentSpec argSpec = m_descriptor.getInputArgumentSpecs().get(argId);
				if ( argSpec != null ) {
					argSpec = MDTInstanceManagerAware.activate(argSpec, manager);
					ElementValues.update(arg, argSpec.readValue());
				}
				inputArguments.put(argId, arg);
			}

			for ( Map.Entry<String, ReferenceArgumentSpec> ent: m_descriptor.getOutputArgumentSpecs().entrySet() ) {
				ent.getValue().activate(manager);
			}
			
			Map<String,SubmodelElement> outputArguments
								= KeyValueFStream.from(opSmDesc.getOutputs())
												.mapValue(SubmodelArgumentDescriptor::getSubmodelElement)
												.toMap();
			
			Map<String,SubmodelElement> outputs = invoke(manager, inputArguments, outputArguments);
			
			// 태스크 기술자에서 출력 변수들이 ElementReference로 지정된 경우에는
			// output 결과를 반영시킨다.
			m_descriptor.updateOutputArguments(manager, outputs, getLogger());

			// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
			TaskUtils.updateLastExecutionTime(manager, m_descriptor, started, getLogger());
			
			return outputs;
		}
		catch ( TimeoutException | InterruptedException | CancellationException | TaskException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}
	
	public Optional<Duration> getTimeout() {
		return m_descriptor.findOptionValue(OPTION_TIMEOUT)
							.map(UnitUtils::parseSecondDuration);
	}
	
	public Optional<Duration> getPollInterval() {
		return m_descriptor.findOptionValue(OPTION_POLL_INTERVAL)
							.map(UnitUtils::parseSecondDuration);
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public Logger getLogger() {
		return Optionals.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
}
