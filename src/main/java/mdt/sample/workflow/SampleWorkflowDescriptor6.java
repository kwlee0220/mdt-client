package mdt.sample.workflow;

import mdt.aas.DefaultSubmodelReference;
import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.workflow.HttpWorkflowManagerProxy;
import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.SubmodelRefOption;
import mdt.model.workflow.WorkflowDescriptors;
import mdt.task.builtin.HttpTask;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleWorkflowDescriptor6 {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987/operations";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect(ENDPOINT);
		HttpMDTInstanceManagerClient manager = mdt.getInstanceManager();
		
		WorkflowDescriptor wfDesc;
		
		wfDesc = new WorkflowDescriptor();
		wfDesc.setId("sample-workflow-6");
		wfDesc.setName("테스트 시뮬레이션");
		wfDesc.setDescription("본 워크플로우는 시뮬레이션 연동을 확인하기 위한 테스트 목적으로 작성됨.");

		TaskDescriptor taskDesc;
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-image",
											"surface/Data/DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
											"surface/표면결함진단/AIInfo.Inputs[0].InputValue");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = createSurfaceErrorDetection(manager, "detect-surface-error");
		taskDesc.getDependencies().add("copy-image");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-error-type",
											"surface/표면결함진단/AIInfo.Outputs[0].OutputValue",
											"surface/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue");
		taskDesc.getDependencies().add("detect-surface-error");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = createSTErrorPrediction(manager, "predict-st-error");
		taskDesc.getDependencies().add("detect-surface-error");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-st-error",
											"surface/단기간불량예측/AIInfo.Outputs[0].OutputValue",
											"surface/Data/DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue");
		taskDesc.getDependencies().add("predict-st-error");
		wfDesc.getTasks().add(taskDesc);

		taskDesc = createLTErrorPrediction(manager, "predict-lt-error");
		taskDesc.getDependencies().add("predict-st-error");
		wfDesc.getTasks().add(taskDesc);
		
		taskDesc = WorkflowDescriptors.newCopyTask("copy-lt-error",
											"surface/단기간불량예측/AIInfo.Outputs[0].OutputValue",
											"surface/Data/DataInfo.Equipment.EquipmentParameterValues[3].ParameterValue");
		taskDesc.getDependencies().add("predict-lt-error");
		wfDesc.getTasks().add(taskDesc);
		
		System.out.println(MDTModelSerDe.toJsonString(wfDesc));
		
		HttpWorkflowManagerProxy wfManager = mdt.getWorkflowManager();
		wfManager.addWorkflowDescriptor(wfDesc);
	}
	
	private static TaskDescriptor createSurfaceErrorDetection(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.getOptions().add(new StringOption("url", HTTP_OP_SERVER_ENDPOINT + "/surface_error_detection"));
		task.getOptions().add(new StringOption("timeout", "5m"));
		task.getOptions().add(new StringOption("logger", "info"));
		task.getOptions().add(new SubmodelRefOption("submodel", "surface", "표면결함진단"));
		task.getLabels().add(NameValue.of("mdt-submodel", "surface/표면결함진단"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("surface", "표면결함진단");
		smRef.activate(manager);
		
		WorkflowDescriptors.addAIInputOutputVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor createSTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.getOptions().add(new StringOption("url", HTTP_OP_SERVER_ENDPOINT + "/st_error_prediction"));
		task.getOptions().add(new StringOption("timeout", "5m"));
		task.getOptions().add(new StringOption("logger", "info"));
		task.getOptions().add(new SubmodelRefOption("submodel", "surface", "단기간불량예측"));
		task.getLabels().add(NameValue.of("mdt-submodel", "surface/단기간불량예측"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("surface", "단기간불량예측");
		smRef.activate(manager);
		
		WorkflowDescriptors.addAIInputOutputVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor createLTErrorPrediction(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		
		task.getOptions().add(new StringOption("url", HTTP_OP_SERVER_ENDPOINT + "/lt_error_prediction"));
		task.getOptions().add(new StringOption("timeout", "5m"));
		task.getOptions().add(new StringOption("logger", "info"));
		task.getOptions().add(new SubmodelRefOption("submodel", "surface", "장시간불량예측"));
		task.getLabels().add(NameValue.of("mdt-submodel", "surface/장시간불량예측"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance("surface", "장시간불량예측");
		smRef.activate(manager);
		
		WorkflowDescriptors.addAIInputOutputVariables(task, smRef);
		
		return task;
	}
}
