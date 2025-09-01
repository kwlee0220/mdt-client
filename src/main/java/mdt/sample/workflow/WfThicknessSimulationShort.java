package mdt.sample.workflow;

import java.io.IOException;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.ModelValidationException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessSimulationShort {
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://218.158.72.211:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("thickness-simulation-short");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor descriptor;
		Variable var;

		descriptor = inspectSurfaceThickness2(manager, "inspect-thickness");
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("UpperImage", "",
																			"param:inspector:UpperImage:ParameterValue"));
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = updateDefectList2(manager, "update-defect-list");
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("Defect", "",
																		"oparg:inspector:ThicknessInspection:out:0"));
		descriptor.getInputVariables().addOrReplace(Variables.newInstance("DefectList", "",
																		"param:inspector:DefectList:ParameterValue"));
		descriptor.getOutputVariables().addOrReplace(Variables.newInstance("DefectList", "",
																		"param:inspector:DefectList:ParameterValue"));
		descriptor.getDependencies().add("inspect-thickness");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = simulateProcess2(manager, "simulate-process");
		var = Variables.newInstance("DefectList", null, "param:inspector:DefectList:ParameterValue");
		descriptor.getInputVariables().addOrReplace(var);
		var = Variables.newInstance("AverageCycleTime", null, "param:inspector:CycleTime:ParameterValue");
		descriptor.getOutputVariables().addOrReplace(var);
		descriptor.getDependencies().add("update-defect-list");
		wfDesc.getTaskDescriptors().add(descriptor);

//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfDesc = wfManager.addOrReplaceWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfDesc.getId());
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id)
		throws ModelValidationException, IOException {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		smRef.activate(manager);
		
		TaskDescriptor descriptor = TaskDescriptors.from(smRef);
		descriptor.addOption("loglevel", "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
	private static TaskDescriptor inspectSurfaceThickness2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.addInputVariable(Variables.newInstance("UpperImage", "",
																		"param:inspector:UpperImage:ParameterValue"))
								.build();
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		smRef.activate(manager);

		return TaskDescriptors.httpTaskBuilder()
								.id(id)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId("inspector/UpdateDefectList")
								.pollInterval("1s")
								.timeout("1m")
								.operationSubmodelRef(smRef)
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}
	private static TaskDescriptor updateDefectList2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}
	
	private static TaskDescriptor simulateProcess2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
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
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}
}
