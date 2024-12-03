package mdt.sample.workflow;

import java.util.Set;

import mdt.aas.DefaultSubmodelReference;
import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.WorkflowDescriptors;
import mdt.task.builtin.HttpTask;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor4 {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("sample-workflow-4");
		wfDesc.setName("테스트 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-data",
												"test/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
												"test/Simulation/SimulationInfo.Inputs[0].InputValue");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-inc-amount",
												"test/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue",
												"test/Simulation/SimulationInfo.Inputs[1].InputValue");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newSetTask("set-sleeptime", "2",
												"test/Simulation/SimulationInfo.Inputs[2].InputValue");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = newHttpTask(manager, "simulation");
		taskDesc.setDependencies(Set.of("copy-data", "copy-inc-amount", "set-sleeptime"));
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-result",
												"test/Simulation/SimulationInfo.Outputs[0].OutputValue",
												"test/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		taskDesc.setDependencies(Set.of("simulation"));
		wfDesc.getTasks().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor newHttpTask(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());

		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "test/Simulation"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
//		task.getOptions().add(new SubmodelRefOption("submodel", "test", "Simulation"));
		task.getLabels().add(NameValue.of("mdt-submodel", "test/Simulation"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("test", "Simulation");
		smRef.activate(manager);
		
		WorkflowDescriptors.addSimulationInputOutputVariables(task, smRef);
		
		return task;
	}
}
