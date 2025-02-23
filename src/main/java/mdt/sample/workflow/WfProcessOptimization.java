package mdt.sample.workflow;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
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
public class WfProcessOptimization {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("innercase-process-optimization");
		wfDesc.setName("내함 성형 공정 최적화");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 성형 공정 최적화을 수행한다.");

		TaskDescriptor taskDesc;

		taskDesc = WorkflowDescriptors.newCopyTask("copy-heater-cycletime",
											"heater/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
											"innercase/ProcessOptimization/SimulationInfo.Inputs[0].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = WorkflowDescriptors.newCopyTask("copy-former-cycletime",
											"former/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
											"innercase/ProcessOptimization/SimulationInfo.Inputs[1].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = WorkflowDescriptors.newCopyTask("copy-trimmer-cycletime",
											"trimmer/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
											"innercase/ProcessOptimization/SimulationInfo.Inputs[2].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = WorkflowDescriptors.newCopyTask("copy-inspector-cycletime",
											"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
											"innercase/ProcessOptimization/SimulationInfo.Inputs[3].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = optimizeProcess(manager, "optimize-process");
		taskDesc.getDependencies().add("copy-heater-cycletime");
		taskDesc.getDependencies().add("copy-former-cycletime");
		taskDesc.getDependencies().add("copy-trimmer-cycletime");
		taskDesc.getDependencies().add("copy-inspector-cycletime");
		wfDesc.getTasks().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor optimizeProcess(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "innercase/ProcessOptimization"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "innercase/ProcessOptimization"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("innercase", "ProcessOptimization");
		smRef.activate(manager);
		
		WorkflowDescriptors.addSimulationInputOutputVariables(task, smRef);
		
		return task;
	}
}
