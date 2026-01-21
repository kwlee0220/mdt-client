package mdt.sample.workflow;

import org.apache.commons.text.StringSubstitutor;

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
public class SampleWorkflowDescriptor3 {
	private static final String WORKFLOW_ID = "sample-workflow-3";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://${LOCAL_HOST}:12987";
	
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
		wfModel = wfManager.addOrReplaceWorkflowModel(wfModel);

		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String taskId) {
		TaskDescriptor task = new TaskDescriptor(taskId, "", HttpTask.class.getName());
		
		StringSubstitutor subst = new StringSubstitutor(System.getenv());
		String resolvedEndpoint = subst.replace(HTTP_OP_SERVER_ENDPOINT);

		task.addOption(HttpTask.OPTION_OPERATION, "test:AddAndSleep");
		task.addOption(HttpTask.OPTION_SERVER_ENDPOINT, resolvedEndpoint);
		task.addOption(HttpTask.OPTION_POLL_INTERVAL, "1.0");
		task.addOption(HttpTask.OPTION_TIMEOUT, "60");
		task.addOption(HttpTask.OPTION_LOG_LEVEL, "info");
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "test:AddAndSleep"));

		task.addInputArgumentSpec("Data", ArgumentSpec.reference("param:test:Data"));
		task.addInputArgumentSpec("IncAmount", ArgumentSpec.literal(11));
		task.addInputArgumentSpec("SleepTime", 
								ArgumentSpec.reference("test:Data:DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue"));
		task.addOutputArgumentSpec("Output", ArgumentSpec.reference("param:test:Data"));
		
		return task;
	}
}
