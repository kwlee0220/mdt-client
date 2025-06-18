package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor1 {
	private static final String WORKFLOW_ID = "sample-workflow-1";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = TaskDescriptors.newSetTaskDescriptor("set", "'222'", "param:test:1:ParameterValue");
		wfModel.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy", "param:test:1:ParameterValue",
														"oparg:test:AddAndSleep:in:1"); 
		taskDesc.getDependencies().add("set");
		
		wfModel.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfModel);
		
		wfModel = wfManager.getWorkflowModel(wfId);
		System.out.println(wfModel.toJsonString());
	}
}
