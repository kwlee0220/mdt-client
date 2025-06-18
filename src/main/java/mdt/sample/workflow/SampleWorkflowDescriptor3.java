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
public class SampleWorkflowDescriptor3 {
	private static final String WORKFLOW_ID = "sample-workflow-3";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://218.158.72.211:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfModel;
		
		wfModel = new WorkflowModel();
		wfModel.setId(WORKFLOW_ID);
		wfModel.setName("테스트 시뮬레이션");
		wfModel.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = newHttpTask(manager, "sleep-and-add");
		wfModel.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfModel);

		wfModel = wfManager.getWorkflowModel(wfId);
		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String taskId) {
		TaskDescriptor task = new TaskDescriptor(taskId, "", HttpTask.class.getName());

		task.addOrReplaceOption(HttpTask.OPTION_OPERATION, "test/AddAndSleep");
		task.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, HTTP_OP_SERVER_ENDPOINT);
		task.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, "1s");
		task.addOrReplaceOption(HttpTask.OPTION_TIMEOUT, "1m");
		task.addOrReplaceOption("loglevel", "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "test:AddAndSleep"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		smRef.activate(manager);
		TaskDescriptors.loadSimulationVariables(task, smRef);
		
		return task;
	}
}
