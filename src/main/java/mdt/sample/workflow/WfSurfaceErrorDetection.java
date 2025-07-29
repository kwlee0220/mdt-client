package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
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
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor SurfaceErrorDetection(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/SurfaceErrorDetection");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:SurfaceErrorDetection"));
		
		task.getInputVariables().add(Variables.newInstance("TestImage", "", "inspector:Data:0"));
		task.getOutputVariables().add(Variables.newInstance("ErrorTypeClass", "", "inspector:Data:1"));
		
		return task;
	}
	
	private static TaskDescriptor STErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/STErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:STErrorPrediction"));

		task.getOutputVariables().add(Variables.newInstance("STErrorPossibility", "", "inspector:Data:2"));
		
		return task;
	}
	
	private static TaskDescriptor LTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor(id, null, HttpTask.class.getName());
		
		task.addOption("server", HTTP_OP_SERVER_ENDPOINT);
		task.addOption("id", "ktech_inspector/LTErrorPrediction");
		task.addOption("timeout", "1m");
		task.addOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "ktech_inspector:LTErrorPrediction"));

		task.getOutputVariables().add(Variables.newInstance("LTErrorPrediction", "", "inspector:Data:3"));
		
		return task;
	}
}
