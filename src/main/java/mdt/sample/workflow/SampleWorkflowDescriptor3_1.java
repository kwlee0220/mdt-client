package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor3_1 {
	private static final String WORKFLOW_ID = "sample-workflow-3-1";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = newHttpTask(manager, "sleep-and-add");
		wfModel.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfModel);

		wfModel = wfManager.getWorkflowModel(wfId);
		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String taskId) {
		TaskDescriptor task = new TaskDescriptor(taskId, "", AASOperationTask.class.getName());
		
		MDTElementReference opRef = DefaultElementReference.newInstance("test", "AddAndSleep", "Operation");
		task.addOrReplaceOption(AASOperationTask.OPTION_OPERATION, opRef);
		task.addOrReplaceOption(AASOperationTask.OPTION_POLL_INTERVAL, "1.0");
		task.addOrReplaceOption(AASOperationTask.OPTION_TIMEOUT, "60");
		task.addOrReplaceOption(AASOperationTask.OPTION_LOG_LEVEL, "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "test:AddAndSleep"));
		
		task.getInputVariables().addOrReplace(Variables.newReferenceVariable("Data", "", "param:test:Data:ParameterValue"));
		task.getInputVariables().addOrReplace(Variables.newValueVariable("IncAmount", "", "7"));
		task.getInputVariables().addOrReplace(Variables.newReferenceVariable("SleepTime", "", "param:test:SleepTime:ParameterValue"));
		task.getOutputVariables().addOrReplace(Variables.newReferenceVariable("Output", "", "param:test:Data"));
		
		return task;
	}
}
