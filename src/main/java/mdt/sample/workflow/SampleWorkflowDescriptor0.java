package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.task.builtin.SetTask;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor0 {
	private static final String WORKFLOW_ID = "sample-workflow-0";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("Set 동작 시험용 task");
		wfModel.setDescription("본 워크플로우는 Set 동작 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setId("set-inc-amount");
		descriptor.setName("증가량 설정");
		descriptor.setDescription("증가량을 설정하는 Task");
		descriptor.setType(SetTask.class.getName());
		
		// option들을 설정한다.
		descriptor.addOption(SetTask.OPTION_LOG_LEVEL, "info");
		
		//
		// 입출력 변수들을 설정한다.
		//
		descriptor.addInputArgumentSpec("source", ArgumentSpec.literal(7));
		descriptor.addOutputArgumentSpec("target", ArgumentSpec.reference("param:test:IncAmount"));
		
		wfModel.getTaskDescriptors().add(descriptor);
		wfModel.getGui().put("layoutSize", 50);
		wfModel.getGui().put("layoutType", "grid");
		
		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfModel = wfManager.addOrReplaceWorkflowModel(wfModel);
		System.out.println(wfModel.toJsonString());
	}
}
