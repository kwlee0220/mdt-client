package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
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

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.addInputVariable("UpperImage", "", "param:inspector:UpperImage")
								.addOutputVariable("Defect", "", "oparg:inspector:ThicknessInspection:out:Defect")
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.addInputVariable(Variables.newInstance("UpperImage", "",
																		"param:inspector:UpperImage"))
								.build();
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.addInputVariable("Defect", "", "oparg:inspector:ThicknessInspection:out:Defect")
								.addInputVariable("DefectList", "", "param:inspector:DefectList")
								.addOutputVariable("DefectList", "", "param:inspector:DefectList")
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}
	
	private static TaskDescriptor simulateProcess(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ProcessSimulation");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.addInputVariable("DefectList", "", "param:inspector:DefectList")
								.addOutputVariable("AverageCycleTime", "", "param:inspector:CycleTime")
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}
	
	private static TaskDescriptor optimizeProcess(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("innercase", "ProcessOptimization");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);

		return TaskDescriptors.aasOperationTaskBuilder()
								.id(id)
								.operationRef(opElmRef)
								.addInputVariable("HTCycleTime", "", "param:heater:CycleTime")
								.addInputVariable("VFCycleTime", "", "param:former:CycleTime")
								.addInputVariable("PTCycleTime", "", "param:trimmer:CycleTime")
								.addInputVariable("QICycleTime", "", "param:inspector:CycleTime")
								.addOutputVariable("TotalThroughput", "", "param:innercase:CycleTime")
								.pollInterval("1s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.build();
	}	
}
