package mdt.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.LoggerSettable;
import utils.func.FOption;

import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractMDTTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractMDTTask.class);
	
	private final TaskDescriptor m_taskDesc;
	private Logger m_logger;

	protected AbstractMDTTask(TaskDescriptor taskDesc) {
		m_taskDesc = taskDesc;
	}
	
	public TaskDescriptor getTaskDescriptor() {
		return m_taskDesc;
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
}
