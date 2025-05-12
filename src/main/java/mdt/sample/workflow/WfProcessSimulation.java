package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.builtin.HttpTask;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.StringOption;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfProcessSimulation {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect(ENDPOINT);
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("inspect-process-simulation");
		wfDesc.setName("내함 불량 검사 공정 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 불량을 따른 공정 시뮬레이션을 수행한다.");

		TaskDescriptor taskDesc;

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-defect-list", "param:inspector:Data:1",
													"oparg:inspector:ProcessSimulation:in:0");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = simulateProcess(manager, "simulate-process");
		taskDesc.getDependencies().add("copy-defect-list");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-avg-cycle-time",
													"oparg:inspector:ProcessSimulation:out:0",
													"param:inspector0");
		taskDesc.getDependencies().add("simulate-process");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor simulateProcess(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "inspector/ProcessSimulation"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-operation", "inspector/ProcessSimulation"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		smRef.activate(manager);
		
		TaskDescriptors.loadSimulationVariables(task, smRef);
		
		return task;
	}
}
