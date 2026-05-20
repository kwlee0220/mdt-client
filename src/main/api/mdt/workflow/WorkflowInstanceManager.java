package mdt.workflow;

import java.util.List;

import mdt.model.ResourceNotFoundException;


/**
 * 워크플로우 인스턴스의 생명주기(시작/중단/일시정지/재개/삭제)와 조회를 관리하는 매니저 인터페이스.
 * <p>
 * 각 워크플로우 인스턴스는 워크플로우 모델로부터 생성되며 고유한 식별자({@code wfId})를 갖는다.
 * 인스턴스 단위 작업은 이 식별자를 통해 수행한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowInstanceManager {
	/**
	 * 현재 관리되는 모든 워크플로우 인스턴스의 식별자 목록을 반환한다.
	 *
	 * @return	워크플로우 인스턴스 식별자 목록. 인스턴스가 없으면 빈 리스트.
	 */
	public List<String> listWorkflowIds();

	/**
	 * 주어진 식별자에 해당하는 워크플로우 인스턴스의 현재 실행 상태를 반환한다.
	 *
	 * @param wfId	워크플로우 인스턴스 식별자.
	 * @return	현재 실행 상태.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public WorkflowStatus getWorkflowStatus(String wfId) throws ResourceNotFoundException;

	/**
	 * 모든 워크플로우 등록정보를 반환한다.
	 *
	 * @return	모든 워크플로우 등록정보 목록.
	 */
	public List<Workflow> getWorkflowAll();

	/**
	 * 주어진 식별자에 해당하는 워크플로우 등록정보를 반환한다.
	 *
	 * @param wfId	워크플로우 식별자.
	 * @return	워크플로우 등록정보 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public Workflow getWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 주어진 워크플로우 모델로부터 새 워크플로우 인스턴스를 생성하고 실행을 시작한다.
	 *
	 * @param wfModelId	실행할 워크플로우 모델의 식별자.
	 * @return	새로 생성되어 시작된 워크플로우 인스턴스의 등록정보.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우 모델이 존재하지 않는 경우.
	 */
	public Workflow startWorkflow(String wfModelId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 워크플로우 인스턴스의 실행을 중단한다.
	 *
	 * @param wfId	중단할 워크플로우 인스턴스 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public void stopWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 워크플로우 인스턴스의 실행을 일시정지한다.
	 * <p>
	 * 일시정지된 인스턴스는 {@link #resumeWorkflow(String)}으로 다시 실행을 재개할 수 있다.
	 *
	 * @param wfId	일시정지할 워크플로우 인스턴스 식별자.
	 * @return	일시정지된 후의 워크플로우 등록정보.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public Workflow suspendWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 일시정지 상태의 워크플로우 인스턴스를 재개한다.
	 *
	 * @param wfId	재개할 워크플로우 인스턴스 식별자.
	 * @return	재개된 후의 워크플로우 등록정보.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public Workflow resumeWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 워크플로우 등록정보를 삭제한다.
	 *
	 * @param wfId	삭제할 워크플로우 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public void removeWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 관리되는 모든 워크플로우 등록정보를 삭제한다.
	 */
	public void removeWorkflowAll();

	/**
	 * 주어진 워크플로우 인스턴스의 특정 pod에서 생성된 실행 로그를 반환한다.
	 *
	 * @param wfId		로그를 조회할 워크플로우 인스턴스 식별자.
	 * @param podName	로그를 조회할 pod의 이름.
	 * @return	pod의 실행 로그 문자열.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우 또는 pod이 존재하지 않는 경우.
	 */
	public String getWorkflowLog(String wfId, String podName) throws ResourceNotFoundException;
}
