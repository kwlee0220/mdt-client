package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.MDTTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessSimulationLong {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
//	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://10.254.91.134:12987";
//	private static final String HTTP_OP_SERVER_ENDPOINT = "http://218.158.72.211:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("thickness-simulation-long");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor descriptor;
		
		
		descriptor = TaskDescriptors.newSetTaskDescriptor("copy-image", "param:inspector:UpperImage:ParameterValue",
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
															"oparg:inspector:UpdateDefectList:out:UpdatedDefectList",
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
		wfDesc = wfManager.addOrReplaceWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfDesc.getId());
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		smRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(HttpTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("UpperImage", ArgumentSpec.reference("param:inspector:UpperImage:ParameterValue"));
		descriptor.addOutputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:Defect"));
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		descriptor.addOption(HttpTask.OPTION_OPERATION, "inspector/ThicknessInspection");
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		smRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(HttpTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:Defect"));
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOutputArgumentSpec("Output", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		descriptor.addOption(HttpTask.OPTION_OPERATION, "inspector/UpdateDefectList");
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
	
	private static TaskDescriptor simulateProcess(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		smRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(HttpTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOutputArgumentSpec("AverageCycleTime", ArgumentSpec.reference("param:inspector:CycleTime"));
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		descriptor.addOption(HttpTask.OPTION_OPERATION, "inspector/ProcessSimulation");
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
}
