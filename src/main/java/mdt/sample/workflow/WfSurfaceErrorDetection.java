package mdt.sample.workflow;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.StringOption;
import mdt.task.builtin.HttpTask;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.VariableDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfSurfaceErrorDetection {
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("surface-error-detection");
		wfDesc.setName("표면 오류 감지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 표면 불량을 탐지한다.");

		TaskDescriptor taskDesc;

		taskDesc = SurfaceErrorDetection(manager, "surface-error-detection");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = STErrorPrediction(manager, "short-term-error-prediction");
		taskDesc.getDependencies().add("surface-error-detection");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = LTErrorPrediction(manager, "long-term-error-prediction");
		taskDesc.getDependencies().add("short-term-error-prediction");
		wfDesc.getTasks().add(taskDesc);
		
		WorkflowDescriptorService wfService = mdt.getWorkflowDescriptorService();
		String wfId = wfService.addOrUpdateWorkflowDescriptor(wfDesc, true);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor SurfaceErrorDetection(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "ktech_inspector/SurfaceErrorDetection"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "ktech_inspector/SurfaceErrorDetection"));

		VariableDescriptor TestImage = VariableDescriptor.parseString("TestImage",
									"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		task.getInputVariables().add(TestImage);

		VariableDescriptor ErrorTypeClass = VariableDescriptor.parseString("ErrorTypeClass",
									"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		task.getOutputVariables().add(ErrorTypeClass);
		
		return task;
	}
	
	private static TaskDescriptor STErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "ktech_inspector/STErrorPrediction"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "ktech_inspector/STErrorPrediction"));

		VariableDescriptor STErrorPossibility = VariableDescriptor.parseString("STErrorPossibility",
									"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue");
		task.getOutputVariables().add(STErrorPossibility);
		
		return task;
	}
	
	private static TaskDescriptor LTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "ktech_inspector/LTErrorPrediction"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of("mdt-submodel", "ktech_inspector/LTErrorPrediction"));

		VariableDescriptor LTErrorPrediction = VariableDescriptor.parseString("LTErrorPrediction",
									"inspector/Data/DataInfo.Equipment.EquipmentParameterValues[3].ParameterValue");
		task.getOutputVariables().add(LTErrorPrediction);
		
		return task;
	}
}
