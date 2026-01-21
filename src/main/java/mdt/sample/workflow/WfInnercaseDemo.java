package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.MDTTask;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfInnercaseDemo {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("innercase-demo-workflow");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor descriptor;

		// UpperImage는 냉장고 내함의 상단 두께 불량을 탐지.
		descriptor = inspectSurfaceThickness(manager, "inspect-thickness");
		wfDesc.addTaskDescriptor(descriptor);

		// 두께 불량 탐지 결과를 DefectList에 누적.
		descriptor = updateDefectList(manager, "update-defect-list");
		descriptor.addDependency("inspect-thickness");
		wfDesc.addTaskDescriptor(descriptor);

		// DefectList를 기반으로 공정 시뮬레이션을 수행하여 평균 사이클 타임을 계산.
		descriptor = simulateProcess(manager, "simulate-process");
		descriptor.addDependency("update-defect-list");
		wfDesc.addTaskDescriptor(descriptor);
		
		// Innercase를 구성하는 Heater, Former, Trimmer의 사이클 타임을 활용하여 공정 최적화를 실행.
		descriptor = optimizeProcess(manager, "optimize-process");
		descriptor.addDependency("simulate-process");
		wfDesc.addTaskDescriptor(descriptor);

//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfDesc = wfManager.addOrReplaceWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfDesc.getId());
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id) {
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
	
	private static TaskDescriptor simulateProcess(MDTInstanceManager manager, String id) {
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
	
	private static TaskDescriptor optimizeProcess(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("innercase", "ProcessOptimization");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		descriptor.setId(id);
		descriptor.addInputArgumentSpec("HTCycleTime", ArgumentSpec.reference("param:heater:CycleTime"));
		descriptor.addInputArgumentSpec("VFCycleTime", ArgumentSpec.reference("param:former:CycleTime"));
		descriptor.addInputArgumentSpec("PTCycleTime", ArgumentSpec.reference("param:trimmer:CycleTime"));
		descriptor.addInputArgumentSpec("QICycleTime", ArgumentSpec.reference("param:inspector:CycleTime"));
		descriptor.addOutputArgumentSpec("TotalThroughput", ArgumentSpec.reference("param:innercase:CycleTime"));
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmRef.toStringExpr());
		descriptor.addOption(MDTTask.OPTION_TIMEOUT, "1m");
		descriptor.addOption(MDTTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOption(MDTTask.OPTION_LOG_LEVEL, "info");
		descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		
		return descriptor;
	}	
}
