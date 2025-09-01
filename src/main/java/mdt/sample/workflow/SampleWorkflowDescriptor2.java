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
public class SampleWorkflowDescriptor2 {
	private static final String WORKFLOW_ID = "sample-workflow-2";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-data", "param:test:Data",
														"oparg:test:AddAndSleep:in:Data");
		wfModel.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-inc-amount", "param:test:IncAmount",
														"oparg:test:AddAndSleep:in:IncAmount");
		wfModel.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("set-sleeptime", "'3'",
														"oparg:test:AddAndSleep:in:SleepTime");
		wfModel.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfModel = wfManager.addOrReplaceWorkflowModel(wfModel);

		System.out.println(wfModel.toJsonString());
	}
}
