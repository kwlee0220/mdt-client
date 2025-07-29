package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor6 {
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";

	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("sample-workflow-6");
		wfDesc.setName("테스트 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-image", "param:ktech_inspector:0",
												"oparg:ktech_inspector:SurfaceErrorDetection:in:0");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = createSurfaceErrorDetection(manager, "detect-surface-error");
		taskDesc.getDependencies().add("copy-image");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-error-type",
												"oparg:ktech_inspector:SurfaceErrorDetection:out:0",
												"param:ktech_inspector:1");
		taskDesc.getDependencies().add("detect-surface-error");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = createSTErrorPrediction(manager, "predict-st-error");
		taskDesc.getDependencies().add("detect-surface-error");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-st-error",
												"oparg:ktech_inspector:STErrorPrediction:out:0",
												"param:ktech_inspector:2");
		taskDesc.getDependencies().add("predict-st-error");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = createLTErrorPrediction(manager, "predict-lt-error");
		taskDesc.getDependencies().add("predict-st-error");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-lt-error",
												"oparg:ktech_inspector:STErrorPrediction:out:0",
												"param:ktech_inspector:3");
		taskDesc.getDependencies().add("predict-lt-error");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor createSurfaceErrorDetection(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());

		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/SurfaceErrorDetection");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:SurfaceErrorDetection"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("ktech_inspector", "SurfaceErrorDetection");
		smRef.activate(manager);
		
		TaskDescriptors.loadAIVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor createSTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());

		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/STErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:STErrorPrediction"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("ktech_inspector", "STErrorPrediction");
		smRef.activate(manager);
		
		TaskDescriptors.loadAIVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor createLTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());

		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/LTErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:LTErrorPrediction"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("ktech_inspector", "LTErrorPrediction");
		smRef.activate(manager);
		
		TaskDescriptors.loadAIVariables(task, smRef);
		
		return task;
	}
}
