package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor4 {
	private static final String WORKFLOW_ID = "sample-workflow-4";
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
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-data")
									.target("oparg:test:AddAndSleep:in:Data")
									.source("param:test:Data:ParameterValue")
									.build();
		wfModel.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-inc-amount")
									.target("oparg:test:AddAndSleep:in:IncAmount")
									.source("param:test:IncAmount:ParameterValue")
									.build();
		wfModel.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("set-sleeptime")
									.target("oparg:test:AddAndSleep:in:SleepTime")
									.value("3")
									.build();
		wfModel.getTaskDescriptors().add(taskDesc);
		
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		smRef.activate(manager);
//		taskDesc = newHttpTask("simulation", smRef);
		taskDesc = newAASOperationTask("sleep-and-add", smRef);
		wfModel.getTaskDescriptors().add(taskDesc);

		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-result")
									.target("param:test:Data:ParameterValue")
									.source("oparg:test:AddAndSleep:out:Output")
									.addDependency("sleep-and-add")
									.build();
		wfModel.getTaskDescriptors().add(taskDesc);

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfModel);

		wfModel = wfManager.getWorkflowModel(wfId);
		System.out.println(wfModel.toJsonString());
	}
	
	private static TaskDescriptor newHttpTask(String taskId, ByIdShortSubmodelReference smRef) {
		return TaskDescriptors.httpTaskBuilder()
								.id(taskId)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId(smRef.getInstanceId() + "/" + smRef.getSubmodelIdShort())
								.pollInterval("3s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.operationSubmodelRef(smRef)
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.addDependency("copy-data", "copy-inc-amount", "set-sleeptime")
								.build();
	}
	
	private static TaskDescriptor newAASOperationTask(String taskId, ByIdShortSubmodelReference smRef) {
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		return TaskDescriptors.aasOperationTaskBuilder()
								.id(taskId)
								.operationRef(opElmRef)
								.pollInterval("3s")
								.timeout("1m")
								.addOption("loglevel", "info")
								.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr())
								.addDependency("copy-data", "copy-inc-amount", "set-sleeptime")
								.build();
	}
}
