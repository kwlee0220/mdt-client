package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.Options;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessSimulationShort {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect(ENDPOINT);
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("thickness-simulation-short");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor descriptor;
		Variable var;

		descriptor = inspectSurfaceThickness(manager, "inspect-thickness");
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("UpperImage", "", "param:inspector:UpperImage"));
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = updateDefectList2(manager, "update-defect-list");
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("Defect", "",
																		"oparg:inspector:ThicknessInspection:out:0"));
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("DefectList", "", "param:inspector:DefectList"));
		descriptor.getOutputVariables().addOrReplace(Variables.newInstance("DefectList", "", "param:inspector:DefectList"));
		descriptor.getDependencies().add("inspect-thickness");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = simulateProcess(manager, "simulate-process");
		var = Variables.newInstance("DefectList", null, "param:inspector:DefectList");
		descriptor.getInputVariables().addOrReplace(var);
		var = Variables.newInstance("AverageCycleTime", null, "param:inspector:CycleTime");
		descriptor.getOutputVariables().addOrReplace(var);
		descriptor.getDependencies().add("update-defect-list");
		wfDesc.getTaskDescriptors().add(descriptor);

//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		smRef.activate(manager);

		return TaskDescriptors.httpTaskBuilder()
								.id(id)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId("inspector/ThicknessInspection")
								.pollInterval("2s")
								.timeout("1m")
								.operationSubmodelRef(smRef)
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.build();
	}
	private static TaskDescriptor inspectSurfaceThickness2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.pollInterval("2s")
								.timeout("1m")
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.addInputVariable(Variables.newInstance("UpperImage", "", "param:inspector:UpperImage"))
								.build();
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		smRef.activate(manager);

		return TaskDescriptors.httpTaskBuilder()
								.id(id)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId("inspector/UpdateDefectList")
								.pollInterval("2s")
								.timeout("1m")
								.operationSubmodelRef(smRef)
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.build();
	}
	private static TaskDescriptor updateDefectList2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.pollInterval("2s")
								.timeout("1m")
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.build();
	}
	
	private static TaskDescriptor simulateProcess(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		smRef.activate(manager);

		return TaskDescriptors.httpTaskBuilder()
								.id(id)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId("inspector/ProcessSimulation")
								.pollInterval("2s")
								.timeout("1m")
								.operationSubmodelRef(smRef)
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.build();
	}
}
