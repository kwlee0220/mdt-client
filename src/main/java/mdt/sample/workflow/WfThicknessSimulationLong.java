package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.builtin.HttpTask;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.Options;
import mdt.workflow.model.StringOption;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessSimulationLong {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
//	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("thickness-simulation-long");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor descriptor;
		
		
		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-image", "param:inspector:UpperImage",
														"oparg:inspector:ThicknessInspection:in:UpperImage");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = inspectSurfaceThickness(manager, "inspect-thickness");
		descriptor.getDependencies().add("copy-image");
		wfDesc.getTaskDescriptors().add(descriptor);
		
		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-defect",
														"oparg:inspector:ThicknessInspection:out:0",
														"inspector:UpdateDefectList:AIInfo.Inputs[0].InputValue");
		descriptor.getDependencies().add("inspect-thickness");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-defect-list", "param:inspector:DefectList",
															"oparg:inspector:UpdateDefectList:in:DefectList");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = updateDefectList(manager, "update-defect-list");
		descriptor.getDependencies().add("copy-defect");
		descriptor.getDependencies().add("copy-defect-list");
		wfDesc.getTaskDescriptors().add(descriptor);
		
		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-updated-defect-list",
															"oparg:inspector:UpdateDefectList:out:DefectList",
															"param:inspector:DefectList");
		descriptor.getDependencies().add("update-defect-list");
		wfDesc.getTaskDescriptors().add(descriptor);
		
		// Phase 2.

		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-defect-list-to-simulator",
															"param:inspector:DefectList",
															"oparg:inspector:ProcessSimulation:in:DefectList");
		descriptor.getDependencies().add("copy-updated-defect-list");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = simulateProcess(manager, "simulate-process");
		descriptor.getDependencies().add("copy-defect-list-to-simulator");
		wfDesc.getTaskDescriptors().add(descriptor);
		
		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-avg-cycle-time",
														"oparg:inspector:ProcessSimulation:out:AverageCycleTime",
														"param:inspector:CycleTime");
		descriptor.getDependencies().add("simulate-process");
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
