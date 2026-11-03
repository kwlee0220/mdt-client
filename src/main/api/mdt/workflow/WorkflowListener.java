package mdt.workflow;

/**
 * 워크플로우 실행 상태 변화를 통보받는 리스너.
 * <p>
 * {@link mdt.workflow.model.WorkflowStatusMonitor}가 워크플로우 상태를 주기적으로 조회하다가 상태가 바뀌면
 * 해당 메소드를 호출한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowListener {
	/**
	 * 워크플로우가 시작 중({@link WorkflowStatus#STARTING}) 상태가 되었을 때 호출된다.
	 * <p>
	 * 시작 중 상태를 관찰하지 못하고 곧바로 실행 중 상태가 관찰된 경우에도
	 * {@link #onWorkflowStarted(String)} 직전에 호출된다.
	 *
	 * @param wfName	워크플로우 이름.
	 */
	public void onWorkflowStarting(String wfName);

	/**
	 * 워크플로우가 실행 중({@link WorkflowStatus#RUNNING}) 상태가 되었을 때 호출된다.
	 *
	 * @param wfName	워크플로우 이름.
	 */
	public void onWorkflowStarted(String wfName);

	/**
	 * 워크플로우가 성공적으로 완료되었을 때 호출된다.
	 *
	 * @param wfName	워크플로우 이름.
	 */
	public void onWorkflowCompleted(String wfName);

	/**
	 * 워크플로우가 실패로 종료되었을 때 호출된다.
	 *
	 * @param wfName	워크플로우 이름.
	 */
	public void onWorkflowFailed(String wfName);

}
