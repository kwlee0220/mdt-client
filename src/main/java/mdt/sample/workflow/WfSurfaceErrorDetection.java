package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfSurfaceErrorDetection {
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect(ENDPOINT);
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("surface-error-detection");
		wfDesc.setName("표면 오류 감지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 표면 불량을 탐지한다.");

		TaskDescriptor taskDesc;

		taskDesc = SurfaceErrorDetection(manager, "surface-error-detection");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = STErrorPrediction(manager, "short-term-error-prediction");
		taskDesc.getDependencies().add("surface-error-detection");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = LTErrorPrediction(manager, "long-term-error-prediction");
		taskDesc.getDependencies().add("short-term-error-prediction");
		wfDesc.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfDesc = wfManager.addOrReplaceWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfDesc.getId());
	}
	
	private static TaskDescriptor SurfaceErrorDetection(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/SurfaceErrorDetection");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:SurfaceErrorDetection"));

		task.addInputArgumentSpec("TestImage", ArgumentSpec.reference("inspector:Data:0"));
		task.addOutputArgumentSpec("ErrorTypeClass", ArgumentSpec.reference("inspector:Data:1"));
		
		return task;
	}
	
	private static TaskDescriptor STErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/STErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:STErrorPrediction"));

		task.addOutputArgumentSpec("STErrorPossibility", ArgumentSpec.reference("inspector:Data:2"));
		
		return task;
	}
	
	private static TaskDescriptor LTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/LTErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:LTErrorPrediction"));

		task.addOutputArgumentSpec("LTErrorPrediction", ArgumentSpec.reference("inspector:Data:3"));
		
		return task;
	}
}
