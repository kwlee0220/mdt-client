package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.Options;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor4 {
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";

	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("sample-workflow-4");
		wfDesc.setName("테스트 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-data")
									.target("oparg:test:Simulation:in:Data")
									.source("param:test:Data")
									.build();
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-inc-amount")
									.target("oparg:test:Simulation:in:IncAmount")
									.source("param:test:IncAmount")
									.build();
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("set-sleeptime")
									.target("oparg:test:Simulation:in:SleepTime")
									.value("3")
									.build();
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		smRef.activate(manager);
//		taskDesc = newHttpTask("simulation", smRef);
		taskDesc = newAASOperationTask("simulation", smRef);
		wfDesc.getTaskDescriptors().add(taskDesc);

		
		taskDesc = TaskDescriptors.setTaskBuilder()
									.id("copy-result")
									.target("param:test:Data")
									.source("oparg:test:Simulation:out:Data")
									.addDependency("simulation")
									.build();
		wfDesc.getTaskDescriptors().add(taskDesc);

		System.out.println(wfDesc.toJsonString());
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
//		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor newHttpTask(String taskId, ByIdShortSubmodelReference smRef) {
		return TaskDescriptors.httpTaskBuilder()
								.id(taskId)
								.serverEndpoint(HTTP_OP_SERVER_ENDPOINT)
								.operationId(smRef.getInstanceId() + "/" + smRef.getSubmodelIdShort())
								.pollInterval("3s")
								.timeout("1m")
								.addOption(Options.newOption("loglevel", "info"))
								.operationSubmodelRef(smRef)
								.addLabel("mdt-operation", smRef.toStringExpr())
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
								.addOption(Options.newOption("loglevel", "info"))
								.addLabel("mdt-operation", smRef.toStringExpr())
								.addDependency("copy-data", "copy-inc-amount", "set-sleeptime")
								.build();
	}
}
