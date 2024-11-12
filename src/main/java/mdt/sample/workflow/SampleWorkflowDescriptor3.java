package mdt.sample.workflow;

import mdt.aas.DefaultSubmodelReference;
import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.workflow.HttpWorkflowManagerProxy;
import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.WorkflowDescriptors;
import mdt.task.builtin.HttpTask;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor3 {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("sample-workflow-3");
		wfDesc.setName("테스트 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;

		taskDesc = newHttpTask(manager, "simulation");
		wfDesc.getTasks().add(taskDesc);
		
		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		HttpWorkflowManagerProxy wfManager = mdt.getWorkflowManager();
		wfManager.addWorkflowDescriptor(wfDesc);
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.getOptions().add(new StringOption("url", "http://129.254.91.134:12987/operations/test"));
		task.getOptions().add(new StringOption("timeout", "5m"));
		task.getOptions().add(new StringOption("logger", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "Test/Simulation"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("Test", "Simulation");
		smRef.activate(manager);
		
		WorkflowDescriptors.addSimulationInputOutputVariables(task, smRef);
		
		return task;
	}
}
