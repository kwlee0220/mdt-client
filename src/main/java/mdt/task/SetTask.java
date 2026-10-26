package mdt.task;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Preconditions;

import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTask implements MDTTask {
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(SetTask.class);
	
	public static final String ARG_SOURCE = "source";
	public static final String ARG_TARGET = "target";
	public static final String OPTION_LOG_LEVEL = "loglevel";
	
	private final TaskArgument m_source;
	private final TaskArgument.ReferenceArgument m_target;
	
	private SetTask(Builder builder) {
		Preconditions.checkArgument(builder.m_source != null, "source argument is not specified");
		Preconditions.checkArgument(builder.m_target != null, "target argument is not specified");
		
		m_source = builder.m_source;
		m_target = builder.m_target;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return null;
	}

	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
			throws InterruptedException, CancellationException, TaskException {
		try {
			if ( m_source instanceof TaskArgument.ReferenceArgument ) {
				((TaskArgument.ReferenceArgument)m_source).activate(manager);
			}
			m_target.activate(manager);

			m_target.updateValue(m_source.readValue());
			return Map.of(ARG_TARGET, m_target.read());
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to set value: cause=" + e, e);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private TaskArgument m_source;
		private TaskArgument.ReferenceArgument m_target;
		
		public Builder() {
		}
		
		public SetTask build() {
			return new SetTask(this);
		}
		
		public Builder source(TaskArgument source) {
			m_source = source;
			return this;
		}
		
		public Builder target(TaskArgument.ReferenceArgument target) {
			m_target = target;
			return this;
		}
	}
}