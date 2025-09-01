package mdt.workflow;

import java.util.List;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowModelManager {
	/**
	 * 모든 워크플로우 모델 등록정보를 반환한다.
	 * 
	 * @return	모든 워크플로우 모델 등록정보 목록.
	 */
	public List<WorkflowModel> getWorkflowModelAll();

	/**
	 * 주어진 식별자에 해당하는 워크플로우 모델 등록정보를 반환한다.
	 * 
	 * @param wfModelId	워크플로우 모델 식별자.
	 * @return	워크플로우 모델 등록정보 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우 모델이 존재하지 않는 경우.
	 */
	public WorkflowModel getWorkflowModel(String wfModelId) throws ResourceNotFoundException;

	/**
	 * 주어진 워크플로우 모델 등록정보를 추가한다.
	 * <p>
	 * 만일 동일한 식별자를 갖는 워크플로우 모델이 이미 존재하는 경우에는 예외가 발생된다.
	 * 
	 * @param desc		추가할 워크플로우 모델 등록정보.
	 * @return	추가된 워크플로우 모델 식별자.
	 * @throws ResourceAlreadyExistsException	동일한 식별자를 갖는 워크플로우 모델이 이미 존재하는 경우.
	 */
	public WorkflowModel addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException;
	
	/**
	 * 주어진 워크플로우 모델 등록정보를 추가하거나 갱신한다.
	 * <p>
	 * 동일한 식별자를 갖는 워크플로우 모델이 이미 존재하는 경우에는 주어진 모델로 갱신되며,
	 * 그렇지 않은 경우에는 새로운 워크플로우 모델로 추가된다.
	 * 
	 * @param desc		추가 또는 갱신할 워크플로우 모델 등록정보.
	 * @return		추가 또는 갱신된 워크플로우 모델 식별자.
	 */
	public WorkflowModel addOrReplaceWorkflowModel(WorkflowModel desc);

	/**
	 * 주어진 식별자에 해당하는 워크플로우 모델 등록정보를 삭제한다.
	 * 
	 * @param wfModelId	삭제할 워크플로우 모델 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우 모델이 존재하지 않는 경우.
	 */
	public void removeWorkflowModel(String wfModelId) throws ResourceNotFoundException;
	
	/**
	 * 모든 워크플로우 모델 등록정보를 삭제한다.
	 */
	public void removeWorkflowModelAll();
	
	public String getWorkflowScript(String wfModelId, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException;
}
