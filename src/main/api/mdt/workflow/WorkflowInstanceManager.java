package mdt.workflow;

import java.util.List;

import mdt.model.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowInstanceManager {
	public void onWorkflowModelAdded(WorkflowModel wfModel) throws MDTWorkflowInstanceManagerException;
	public void onWorkflowModelRemoved(String wfModelId) throws MDTWorkflowInstanceManagerException;
	
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
	
	public Workflow startWorkflow(String wfModelId) throws ResourceNotFoundException;
	public void stopWorkflow(String wfId) throws ResourceNotFoundException;
	
	public Workflow suspendWorkflow(String wfId) throws ResourceNotFoundException;
	public Workflow resumeWorkflow(String wfId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 워크플로우 등록정보를 삭제한다.
	 * 
	 * @param wfId	삭제할 워크플로우 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우가 존재하지 않는 경우.
	 */
	public void removeWorkflow(String wfId) throws ResourceNotFoundException;
	
	public void removeWorkflowAll();
	
	public String getWorkflowLog(String wfId, String podName) throws ResourceNotFoundException;
}
