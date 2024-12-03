package mdt.sample.workflow;

import mdt.client.HttpMDTManagerClient;
import mdt.model.workflow.WorkflowDescriptors;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor2 {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("sample-workflow-2");
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
		
		taskDesc = WorkflowDescriptors.newSetTask("set-sleep-time", "3",
												"test/Simulation/SimulationInfo.Inputs[2].InputValue");
		wfDesc.getTasks().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
		System.out.println("Workflow id: " + wfId);
	}
}
