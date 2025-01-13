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
import mdt.workflow.model.VariableDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessSimulationShort {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("thickness-simulation-short");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor taskDesc;
		VariableDescriptor varDesc;

		taskDesc = inspectSurfaceThickness(manager, "inspect-thickness");
		VariableDescriptor from = VariableDescriptor.parseString("UpperImage",
									"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue");
		taskDesc.getInputVariables().replace(from);
		wfDesc.getTasks().add(taskDesc);

		taskDesc = updateDefectList(manager, "update-defect-list");
		varDesc = VariableDescriptor.parseString("Defect",
										"inspector/ThicknessInspection/AIInfo.Outputs[0].OutputValue");
		taskDesc.getInputVariables().replace(varDesc);
		varDesc = VariableDescriptor.parseString("DefectList",
										"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		taskDesc.getInputVariables().replace(varDesc);
		varDesc = VariableDescriptor.parseString("DefectList",
										"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		taskDesc.getOutputVariables().replace(varDesc);
		taskDesc.getDependencies().add("inspect-thickness");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = simulateProcess(manager, "simulate-process");
		varDesc = VariableDescriptor.parseString("DefectList",
										"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		taskDesc.getInputVariables().replace(varDesc);
		varDesc = VariableDescriptor.parseString("AverageCycleTime",
										"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		taskDesc.getOutputVariables().replace(varDesc);
		taskDesc.getDependencies().add("update-defect-list");
		wfDesc.getTasks().add(taskDesc);

//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "inspector/ThicknessInspection"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "inspector/ThicknessInspection"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("inspector", "ThicknessInspection");
		smRef.activate(manager);
		
		WorkflowDescriptors.addAIInputOutputVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "inspector/UpdateDefectList"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "inspector/UpdateDefectList"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("inspector", "UpdateDefectList");
		smRef.activate(manager);
		
		WorkflowDescriptors.addAIInputOutputVariables(task, smRef);
		
		return task;
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
