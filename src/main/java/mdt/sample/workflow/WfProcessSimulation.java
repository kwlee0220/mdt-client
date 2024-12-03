package mdt.sample.workflow;

import mdt.aas.DefaultSubmodelReference;
import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.WorkflowDescriptors;
import mdt.task.builtin.HttpTask;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfProcessSimulation {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("inspect-process-simulation");
		wfDesc.setName("내함 불량 검사 공정 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 불량을 따른 공정 시뮬레이션을 수행한다.");

		TaskDescriptor taskDesc;

		taskDesc = WorkflowDescriptors.newCopyTask("copy-defect-list",
											"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue",
											"inspector/ProcessSimulation/SimulationInfo.Inputs[0].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = simulateProcess(manager, "simulate-process");
		taskDesc.getDependencies().add("copy-defect-list");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-avg-cycle-time",
											"inspector/ProcessSimulation/SimulationInfo.Outputs[0].OutputValue",
											"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		taskDesc.getDependencies().add("simulate-process");
		wfDesc.getTasks().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
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
		task.getLabels().add(NameValue.of("mdt-submodel", "inspector/ProcessSimulation"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("inspector", "ProcessSimulation");
		smRef.activate(manager);
		
		WorkflowDescriptors.addSimulationInputOutputVariables(task, smRef);
		
		return task;
	}
}
