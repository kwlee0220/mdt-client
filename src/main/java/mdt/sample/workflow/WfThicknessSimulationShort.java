package mdt.sample.workflow;

import java.io.IOException;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.ModelValidationException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.variable.Variable;
import mdt.task.MDTTask;
import mdt.task.builtin.AASOperationTask;
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
		descriptor.addInputArgumentSpec("UpperImage", ArgumentSpec.reference("param:inspector:UpperImage"));
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = updateDefectList2(manager, "update-defect-list");
		descriptor.addInputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:0"));
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOutputArgumentSpec("Output", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.getDependencies().add("inspect-thickness");
		wfDesc.getTaskDescriptors().add(descriptor);

		descriptor = simulateProcess2(manager, "simulate-process");
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList:ParameterValue"));
		descriptor.addOutputArgumentSpec("AverageCycleTime", ArgumentSpec.reference("param:inspector:CycleTime:ParameterValue"));
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
		descriptor.setType(HttpTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("UpperImage", ArgumentSpec.reference("param:inspector:UpperImage"));
		descriptor.addOutputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:Defect"));
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		descriptor.addOption(HttpTask.OPTION_OPERATION, "inspector/ThicknessInspection");
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
	private static TaskDescriptor inspectSurfaceThickness2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("UpperImage", ArgumentSpec.reference("param:inspector:UpperImage"));
		descriptor.addOutputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:Defect"));
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmRef.toStringExpr());
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
	private static TaskDescriptor updateDefectList2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("Defect", ArgumentSpec.reference("oparg:inspector:ThicknessInspection:out:Defect"));
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOutputArgumentSpec("Output", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmRef.toStringExpr());
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}
	
	private static TaskDescriptor simulateProcess2(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("DefectList", ArgumentSpec.reference("param:inspector:DefectList"));
		descriptor.addOutputArgumentSpec("AverageCycleTime", ArgumentSpec.reference("param:inspector:CycleTime"));
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmRef.toStringExpr());
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
