package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfProcessOptimization {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect(ENDPOINT);
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("innercase-process-optimization");
		wfDesc.setName("내함 성형 공정 최적화");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 성형 공정 최적화을 수행한다.");

		TaskDescriptor taskDesc;

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-heater-cycletime",
													"param:heater:0",
													"oparg:innercase:ProcessOptimization:in:0");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-former-cycletime",
													"param:former:0",
													"oparg:innercase:ProcessOptimization:in:1");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-trimmer-cycletime",
													"param:trimmer:0",
													"oparg:innercase:ProcessOptimization:in:2");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-inspector-cycletime",
													"param:inspector:0",
													"oparg:innercase:ProcessOptimization:in:3");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = optimizeProcess(manager, "optimize-process");
		taskDesc.getDependencies().add("copy-heater-cycletime");
		taskDesc.getDependencies().add("copy-former-cycletime");
		taskDesc.getDependencies().add("copy-trimmer-cycletime");
		taskDesc.getDependencies().add("copy-inspector-cycletime");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor optimizeProcess(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "innercase/ProcessOptimization");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "innercase:ProcessOptimization"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("innercase", "ProcessOptimization");
		smRef.activate(manager);
		
		TaskDescriptors.loadSimulationVariables(task, smRef);
		
		return task;
	}
}
