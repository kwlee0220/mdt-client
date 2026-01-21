package mdt.workflow.model;

import java.time.Duration;
import java.util.Optional;

import utils.async.PeriodicLoopExecution;
import utils.func.Unchecked;

import mdt.workflow.Workflow;
import mdt.workflow.WorkflowListener;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowStatus;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WorkflowStatusMonitor extends PeriodicLoopExecution<Void> {
    private final WorkflowManager m_wfManager;
    private final String m_wfName;
    private final WorkflowListener m_listener;
    private volatile WorkflowStatus m_status = WorkflowStatus.NOT_STARTED;
    
	public WorkflowStatusMonitor(WorkflowManager wfManager, String wfName, WorkflowListener listener,
								Duration interval, boolean cumulativeInterval) {
		super(interval, cumulativeInterval);
		
		m_wfManager = wfManager;
		m_wfName = wfName;
		m_listener = listener;
	}
	
	public WorkflowStatus getStatus() {
		return m_status;
	}
	
	@Override
	protected void finalizeLoop() throws Exception {
		getLogger().info("[{}]: finalize loop for monitoring workflow: {} ({})",
							getClass().getSimpleName(), m_wfName, m_status);
	}

	@Override
	protected Optional<Void> performPeriodicAction(long loopIndex) throws Exception {
		Workflow workflow = m_wfManager.getWorkflow(m_wfName);
		WorkflowStatus status = workflow.getStatus();
		getLogger().debug("[#%03d] Workflow: {}, status={}", loopIndex, m_wfName, status);
		
		status = statusChanged(status);
		if ( isFinished(status) ) {
			return null;
		}
		else {
			return Optional.empty();
		}
	}
	
	private WorkflowStatus statusChanged(WorkflowStatus status) {
		if ( m_status == status ) {
			return m_status;
		}
		
		switch ( status ) {
			case FAILED:
				if ( !isFinished(m_status) ) {
					m_listener.onWorkflowFailed(m_wfName);
				}
				break;
			case COMPLETED:
				if ( !isFinished(m_status) ) {
					m_listener.onWorkflowCompleted(m_wfName);
				}
				break;
			case RUNNING:
				if ( m_status == WorkflowStatus.NOT_STARTED ) {
					Unchecked.acceptOrIgnore(m_wfName, m_listener::onWorkflowStarting);
					Unchecked.acceptOrIgnore(m_wfName, m_listener::onWorkflowStarted);
				}
				else if ( m_status == WorkflowStatus.STARTING ) {
					Unchecked.acceptOrIgnore(m_wfName, m_listener::onWorkflowStarted);
				}
				else {
					throw new IllegalStateException("Unexpected workflow status: " + m_status);
				}
				break;
			case NOT_STARTED:
				if ( m_status != WorkflowStatus.NOT_STARTED ) {
					throw new IllegalStateException("Unexpected workflow status: " + m_status);
				}
				break;
			case STARTING:
				if ( m_status == WorkflowStatus.NOT_STARTED ) {
					Unchecked.acceptOrIgnore(m_wfName, m_listener::onWorkflowStarting);
				}
				else {
					throw new IllegalStateException("Unexpected workflow status: " + m_status);
				}
				break;
			default:
				throw new IllegalStateException("Unexpected workflow status: " + m_status);
		}
		m_status = status;
		
		return m_status;
	}
	
	private boolean isFinished(WorkflowStatus status) {
		return status == WorkflowStatus.COMPLETED || status == WorkflowStatus.FAILED;
	}
}
