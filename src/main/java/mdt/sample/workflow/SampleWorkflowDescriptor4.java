package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.task.MDTTask;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.SetTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor4 {
	private static final String WORKFLOW_ID = "sample-workflow-4";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://218.158.72.211:12987";

	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");
		
		TaskDescriptor descriptor;
		
		descriptor = new TaskDescriptor();
		descriptor.setType(SetTask.class.getName());
		descriptor.setId("copy-data");
		descriptor.addInputArgumentSpec("source", ArgumentSpec.reference("param:test:Data"));
		descriptor.addOutputArgumentSpec("target", ArgumentSpec.reference("oparg:test:AddAndSleep:in:Data"));
		wfModel.getTaskDescriptors().add(descriptor);

		descriptor = new TaskDescriptor();
		descriptor.setType(SetTask.class.getName());
		descriptor.setId("copy-inc-amount");
		descriptor.addInputArgumentSpec("source", ArgumentSpec.reference("param:test:IncAmount"));
		descriptor.addOutputArgumentSpec("target", ArgumentSpec.reference("oparg:test:AddAndSleep:in:IncAmount"));
		wfModel.getTaskDescriptors().add(descriptor);
		
		descriptor = new TaskDescriptor();
		descriptor.setType(SetTask.class.getName());
		descriptor.setId("set-sleeptime");
		descriptor.addInputArgumentSpec("source", ArgumentSpec.literal(3));
		descriptor.addOutputArgumentSpec("target", ArgumentSpec.reference("oparg:test:AddAndSleep:in:SleepTime"));
		wfModel.getTaskDescriptors().add(descriptor);
		
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		smRef.activate(manager);
//		taskDesc = newHttpTask("simulation", smRef);
		descriptor = newAASOperationTask("sleep-and-add", smRef);
		wfModel.getTaskDescriptors().add(descriptor);
		
		descriptor = new TaskDescriptor();
		descriptor.setType(SetTask.class.getName());
		descriptor.setId("copy-result");
		descriptor.addInputArgumentSpec("source", ArgumentSpec.reference("oparg:test:AddAndSleep:out:Output"));
		descriptor.addOutputArgumentSpec("target", ArgumentSpec.reference("param:test:Data"));
		descriptor.addDependency("sleep-and-add");
		wfModel.getTaskDescriptors().add(descriptor);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfModel = wfManager.addOrReplaceWorkflowModel(wfModel);

		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(String taskId, ByIdShortSubmodelReference smRef) {
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setId(taskId);
		descriptor.setType(HttpTask.class.getName());
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		descriptor.addOption(HttpTask.OPTION_OPERATION, smRef.getInstanceId() + "/" + smRef.getSubmodelIdShort());
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "im");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "3s");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		descriptor.addDependency("copy-data", "copy-inc-amount", "set-sleeptime");
		
		return descriptor;
	}
	
	private static TaskDescriptor newAASOperationTask(String taskId, ByIdShortSubmodelReference smRef) {
		String opElmExpr = DefaultElementReference.newInstance(smRef, "Operation").toStringExpr();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setId(taskId);
		descriptor.setType(AASOperationTask.class.getName());
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmExpr);
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "im");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "3s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		descriptor.addDependency("copy-data", "copy-inc-amount", "set-sleeptime");
		
		return descriptor;
	}
}
