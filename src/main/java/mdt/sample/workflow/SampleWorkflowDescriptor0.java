package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.SetTask;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
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
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

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
		Variable src = Variables.newInstance("source", null, "7");
		descriptor.getInputVariables().addOrReplace(src);
		
		ElementReference incAmountRef = ElementReferences.parseExpr("param:test:IncAmount");
		Variable tar = Variables.newInstance("target", null, incAmountRef);
		descriptor.getOutputVariables().addOrReplace(tar);
		
		wfModel.getTaskDescriptors().add(descriptor);
		
		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfModel);
		
		wfModel = wfManager.getWorkflowModel(wfId);
		System.out.println(wfModel.toJsonString());
	}
}
