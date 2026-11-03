package mdt.workflow.model;

import java.time.Duration;

import utils.async.IntervalMode;
import utils.async.PeriodicLoopExecution;
import utils.func.FOption;
import utils.func.Unchecked;

import mdt.workflow.Workflow;
import mdt.workflow.WorkflowListener;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowStatus;


/**
 * 워크플로우의 실행 상태를 주기적으로 조회하여 상태 변화를 {@link WorkflowListener}에 통보하는 실행.
 * <p>
 * 매 주기마다 {@link WorkflowManager#getWorkflow(String)}로 워크플로우 상태를 확인하고, 이전 상태와 다르면
 * 해당 리스너 메소드를 호출한다. 워크플로우가 {@link WorkflowStatus#COMPLETED} 또는
 * {@link WorkflowStatus#FAILED} 상태가 되면 리스너 통보 후 monitoring을 끝내며, 이 실행은 워크플로우의
 * 성공 여부와 무관하게 정상 완료(결과 {@code null})된다. 예상하지 못한 상태 전이가 관찰되면
 * {@link IllegalStateException}으로 실패한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WorkflowStatusMonitor extends PeriodicLoopExecution<Void> {
    private final WorkflowManager m_wfManager;
    private final String m_wfName;
    private final WorkflowListener m_listener;
    private volatile WorkflowStatus m_status = WorkflowStatus.NOT_STARTED;
    
	/**
	 * 워크플로우 상태 monitor를 만든다. 실행은 {@code start()} 또는 {@code run()}으로 시작한다.
	 *
	 * @param wfManager	워크플로우 상태를 조회할 관리자.
	 * @param wfName	감시할 워크플로우 이름.
	 * @param listener	상태 변화를 통보받을 리스너.
	 * @param interval	상태 조회 주기.
	 * @param cumulativeInterval	{@code true}이면 고정 주기(fixed-rate), {@code false}이면 고정 지연
	 * 							(fixed-delay)으로 조회한다({@link IntervalMode#fromCumulative(boolean)}).
	 */
	public WorkflowStatusMonitor(WorkflowManager wfManager, String wfName, WorkflowListener listener,
								Duration interval, boolean cumulativeInterval) {
		super(interval, IntervalMode.fromCumulative(cumulativeInterval));
		
		m_wfManager = wfManager;
		m_wfName = wfName;
		m_listener = listener;
	}
	
	/**
	 * 마지막으로 관찰된 워크플로우 상태를 반환한다.
	 *
	 * @return	워크플로우 상태. 아직 조회 전이면 {@link WorkflowStatus#NOT_STARTED}.
	 */
	public WorkflowStatus getStatus() {
		return m_status;
	}
	
	@Override
	protected void finalizeLoop() {
		getLogger().info("[{}]: finalize loop for monitoring workflow: {} ({})",
							getClass().getSimpleName(), m_wfName, m_status);
	}

	@Override
	protected FOption<Void> performPeriodicAction(long loopIndex) {
		Workflow workflow = m_wfManager.getWorkflow(m_wfName);
		WorkflowStatus status = workflow.getStatus();
		getLogger().debug("[#{}] Workflow: {}, status={}", loopIndex, m_wfName, status);

		status = statusChanged(status);
		if ( isFinished(status) ) {
			// FOption.of(null)은 '값(null)이 있음'이다. 루프를 끝내고 null 결과로 정상 완료한다.
			return FOption.of(null);
		}
		else {
			return FOption.empty();
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
