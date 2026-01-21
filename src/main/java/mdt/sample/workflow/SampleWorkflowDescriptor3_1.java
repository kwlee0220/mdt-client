package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


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
		wfModel = wfManager.addOrReplaceWorkflowModel(wfModel);

		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String taskId) {
		TaskDescriptor descriptor = new TaskDescriptor(taskId, "", HttpTask.class.getName());
		
		MDTElementReference opRef = DefaultElementReference.newInstance("test", "AddAndSleep", "Operation");
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opRef.toStringExpr());
		descriptor.addOption(AASOperationTask.OPTION_POLL_INTERVAL, "1.0");
		descriptor.addOption(AASOperationTask.OPTION_TIMEOUT, "60");
		descriptor.addOption(AASOperationTask.OPTION_LOG_LEVEL, "info");
		descriptor.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "test:AddAndSleep"));
		
		descriptor.addInputArgumentSpec("Data", ArgumentSpec.reference("param:test:Data"));
		descriptor.addInputArgumentSpec("IncAmount", ArgumentSpec.literal(7));
		descriptor.addInputArgumentSpec("SleepTime", ArgumentSpec.reference("param:test:SleepTime"));
		descriptor.addOutputArgumentSpec("Output", ArgumentSpec.reference("param:test:Data"));
		
		return descriptor;
	}
}
