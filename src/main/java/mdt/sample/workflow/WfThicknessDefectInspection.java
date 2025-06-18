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
import mdt.workflow.model.StringOption;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfThicknessDefectInspection {
//	private static final String ENDPOINT = "http://129.254.91.134:12985";
	private static final String ENDPOINT = "http://localhost:12985";
	private static final String HTTP_OP_SERVER_ENDPOINT = "http://129.254.91.134:12987";
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect(ENDPOINT);
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("thickness-defect-inspection");
		wfDesc.setName("냉장고 내함 두께 불량을 탐지 워크플로우");
		wfDesc.setDescription("본 워크플로우는 냉장고의 내함 두께 불량을 탐지한다.");

		TaskDescriptor taskDesc;
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-image", "param:inspector:Data:2",
															"oparg:inspector:ThicknessInspection:in:0");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = inspectSurfaceThickness(manager, "inspect-thickness");
		taskDesc.getDependencies().add("copy-image");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-defect",
															"oparg:inspector:ThicknessInspection:out:0",
															"oparg:inspector:UpdateDefectList:in:0");
		taskDesc.getDependencies().add("inspect-thickness");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-defect-list", "param:inspector:Data:1",
															"oparg:inspector:UpdateDefectList:in:1");
		wfDesc.getTaskDescriptors().add(taskDesc);

		taskDesc = updateDefectList(manager, "update-defect-list");
		taskDesc.getDependencies().add("copy-defect");
		taskDesc.getDependencies().add("copy-defect-list");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
		taskDesc = TaskDescriptors.newSetTaskDescriptor("copy-updated-defect-list",
															"oparg:inspector:UpdateDefectList:out:0",
															"param:inspector:Data:1");
		taskDesc.getDependencies().add("update-defect-list");
		wfDesc.getTaskDescriptors().add(taskDesc);
		
//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		String wfId = wfManager.addOrUpdateWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfId);
	}
	
	private static TaskDescriptor inspectSurfaceThickness(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "inspector/ThicknessInspection"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "inspector:ThicknessInspection"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		smRef.activate(manager);
		
		TaskDescriptors.loadAIVariables(task, smRef);
		
		return task;
	}
	
	private static TaskDescriptor updateDefectList(MDTInstanceManager manager, String id) {
		TaskDescriptor task = new TaskDescriptor();
		
		task.setId(id);
		task.setType(HttpTask.class.getName());
		task.getOptions().add(new StringOption("server", HTTP_OP_SERVER_ENDPOINT));
		task.getOptions().add(new StringOption("id", "inspector/UpdateDefectList"));
		task.getOptions().add(new StringOption("timeout", "1m"));
		task.getOptions().add(new StringOption("loglevel", "info"));
		task.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "inspector:UpdateDefectList"));
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "UpdateDefectList");
		smRef.activate(manager);
		
		TaskDescriptors.loadAIVariables(task, smRef);
		
		return task;
	}
}
