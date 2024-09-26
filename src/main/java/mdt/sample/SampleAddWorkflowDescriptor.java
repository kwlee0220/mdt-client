package mdt.sample;

import java.util.List;

import mdt.client.workflow.HttpWorkflowManagerClient;
import mdt.model.NameValue;
import mdt.model.ResourceNotFoundException;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.ParameterDescriptor;
import mdt.model.workflow.descriptor.TaskDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;
import mdt.model.workflow.descriptor.port.SubmodelElementPortDescriptor;
import mdt.task.builtin.CopyTask;
import mdt.task.builtin.HttpTask;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleAddWorkflowDescriptor {
	private static final String ENDPOINT = "http://localhost:12985/workflow-manager";
	
	public static final void main(String... args) throws Exception {
		HttpWorkflowManagerClient manager = HttpWorkflowManagerClient.builder()
																	.endpoint(ENDPOINT)
																	.build();

		WorkflowDescriptor wfDesc;
		try {
			wfDesc = manager.getWorkflowDescriptor("process-optimization");
			manager.removeWorkflowDescriptor("process-optimization");
		}
		catch ( ResourceNotFoundException e ) { }
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("process-optimization");
		wfDesc.setName("내함 성형 최적화 공정");
		wfDesc.setDescription("본 워크플로우는 여러 설비로 구성된 내함 성형 공정 과정을 최적화를 시뮬레이션한다.");
		
		wfDesc.getParameters().add(new ParameterDescriptor("simulation-twin", "내함 성형 공정 트윈 식별자"));
		wfDesc.getParameters().add(new ParameterDescriptor("simulation-submodel",
															"내함 성형 공정의 시뮬레이션 Submodel의 idShort"));
		
		wfDesc.getTaskTemplates().add(buildCopyTaskTemplate());
		wfDesc.getTaskTemplates().add(buildHttpTaskTemplate());
		
		
		
		wfDesc.getTasks().add(new TaskDescriptor("copy-heater-cycletime", "copy-cycletime", null,
												List.of(NameValue.of("equipment-twin", "KRCW-01ELQI005"),
														NameValue.of("tar-input-index", "0")), List.of(), List.of()));
		wfDesc.getTasks().add(new TaskDescriptor("copy-vacuum-cycletime", "copy-cycletime", null,
												List.of(NameValue.of("equipment-twin", "KRCW-01ELQI005"),
														NameValue.of("tar-input-index", "1")), List.of(), List.of()));
		wfDesc.getTasks().add(new TaskDescriptor("copy-piercing-cycletime", "copy-cycletime", null,
												List.of(NameValue.of("equipment-twin", "KRCW-01ESUM006"),
														NameValue.of("tar-input-index", "2")), List.of(), List.of()));
		wfDesc.getTasks().add(new TaskDescriptor("copy-inspection-cycletime", "copy-cycletime", null,
												List.of(NameValue.of("equipment-twin", "KRCW-01ETHT006"),
														NameValue.of("tar-input-index", "3")), List.of(), List.of()));
		wfDesc.getTasks().add(new TaskDescriptor("process-optimization", "InnerCase",
												List.of("copy-heater-cycletime", "copy-vacuum-cycletime",
														"copy-piercing-cycletime", "copy-inspection-cycletime"),
												List.of(NameValue.of("equipment-twin", "KRCW-01ETHT006"),
														NameValue.of("tar-input-index", "3")), List.of(), List.of()));
		
		System.out.println(wfDesc);
		for ( TaskTemplateDescriptor desc: wfDesc.getTaskTemplates() ) {
			System.out.println(desc);
		}
		String wfId = manager.addWorkflowDescriptor(wfDesc);
		System.out.println("Workflow added: " + wfId);
		
//		wfDesc = manager.getWorkflowDescriptor(wfId);
//		System.out.println("Workflow found: " + wfId);
//		
//		manager.removeWorkflowDescriptor(wfId);
//		System.out.println("Workflow removed: " + wfId);
	}
	
	private static TaskTemplateDescriptor buildCopyTaskTemplate() {
		TaskTemplateDescriptor desc = new TaskTemplateDescriptor();
		desc.setId("copy-cycletime");
		desc.setName("Cycle time 데이터 준비");
		desc.setType(CopyTask.class.getName());
		desc.setDescription("Process optimization task 수행을 위한 설비의 cycletime 데이터 복사");
		
		desc.getParameters().add(new ParameterDescriptor("equipment-twin", "설비 트윈 식별자"));
		desc.getParameters().add(new ParameterDescriptor("simulation-input-index", "시뮬레이션 입력 정보 순서"));

		SubmodelElementPortDescriptor input = new SubmodelElementPortDescriptor();
		input.setName("input");
		input.setMdtId("${equipment-twin}");
		input.setSubmodelIdShort("Data");
		input.setSmeIdShortPath("DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		desc.getInputPorts().add(input);
		
		SubmodelElementPortDescriptor output = new SubmodelElementPortDescriptor();
		output.setName("output");
		input.setMdtId("${equipment-twin}");
		input.setSubmodelIdShort("${simulation-submodel}");
		input.setSmeIdShortPath("SimulationInfo.Inputs[${simulation-input-index}].InputValue");
		desc.getOutputPorts().add(output);
		
		return desc;
	}
	
	private static TaskTemplateDescriptor buildHttpTaskTemplate() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("InnerCase");
		tmplt.setName("내함 성형 공정 최적화 태스크");
		tmplt.setType(HttpTask.class.getName());
		tmplt.setDescription("Process optimization task");
		
		tmplt.getParameters().add(new ParameterDescriptor("simulation-server-endpoint", "시뮬레이션 서버의 Endpoint"));
		
		tmplt.getOptions().add(new OptionDescriptor("url", true, null, "${simulation-server-endpoint}"));
		tmplt.getOptions().add(new OptionDescriptor("timeout", false, null, "5m"));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, null, "info"));
		
		SubmodelElementPortDescriptor desc;
		
		desc = new SubmodelElementPortDescriptor();
		desc.setName("heater");
		desc.setMdtId("${simulation-twin}");
		desc.setSubmodelIdShort("${simulation-submodel}");
		desc.setSmeIdShortPath("SimulationInfo.Inputs[0].InputValue");
		tmplt.getInputPorts().add(desc);

		desc = new SubmodelElementPortDescriptor();
		desc.setName("vacuum");
		desc.setMdtId("${simulation-twin}");
		desc.setSubmodelIdShort("${simulation-submodel}");
		desc.setSmeIdShortPath("SimulationInfo.Inputs[1].InputValue");
		tmplt.getInputPorts().add(desc);

		desc = new SubmodelElementPortDescriptor();
		desc.setName("piercing");
		desc.setMdtId("${simulation-twin}");
		desc.setSubmodelIdShort("${simulation-submodel}");
		desc.setSmeIdShortPath("SimulationInfo.Inputs[2].InputValue");
		tmplt.getInputPorts().add(desc);

		desc = new SubmodelElementPortDescriptor();
		desc.setName("inspection");
		desc.setMdtId("${simulation-twin}");
		desc.setSubmodelIdShort("${simulation-submodel}");
		desc.setSmeIdShortPath("SimulationInfo.Inputs[3].InputValue");
		tmplt.getInputPorts().add(desc);

		desc = new SubmodelElementPortDescriptor();
		desc.setName("TotalThroughput");
		desc.setMdtId("${simulation-twin}");
		desc.setSubmodelIdShort("${simulation-submodel}");
		desc.setSmeIdShortPath("SimulationInfo.Outputs[0].OutputValue");
		tmplt.getOutputPorts().add(desc);
		
		return tmplt;
	}
}
