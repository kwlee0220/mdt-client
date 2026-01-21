package mdt.sample.workflow;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.TaskUtils;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class WfPredictProductivity {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		WorkflowModel wfDesc;
		
		wfDesc = new WorkflowModel();
		wfDesc.setId("predict-productivity");
		wfDesc.setName("생산성 예측");
		wfDesc.setDescription("실시간 노즐 생산 정보를 활용하여 생산성을 예측하는 워크플로우");

		TaskDescriptor descriptor = predictProductivity(manager, "predict-productivity");
		wfDesc.getTaskDescriptors().add(descriptor);

//		System.out.println(MDTModelSerDe.toJsonString(wfDesc));

		WorkflowManager wfManager = mdt.getWorkflowManager();
		wfDesc = wfManager.addOrReplaceWorkflowModel(wfDesc);
		
		System.out.println("Workflow id: " + wfDesc.getId());
	}
	
	private static TaskDescriptor predictProductivity(MDTInstanceManager manager, String id) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("welder", "ProductivityPrediction");
		DefaultElementReference opElmRef = DefaultElementReference.newInstance(smRef, "Operation");
		opElmRef.activate(manager);
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setId(id);
		descriptor.setName("생산성 예측");
		descriptor.setDescription("생산성 예측을 위한 Task");
		descriptor.setType(AASOperationTask.class.getName());
		
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, opElmRef.toStringExpr());
		descriptor.addOption(AASOperationTask.OPTION_POLL_INTERVAL, "1.0");
		descriptor.addOption(AASOperationTask.OPTION_TIMEOUT, "60");
		descriptor.addOption(AASOperationTask.OPTION_LOG_LEVEL, "info");
		descriptor.getLabels().add(NameValue.of(TaskUtils.LABEL_MDT_OPERATION, "welder:ProductivityPrediction"));

		descriptor.addInputArgumentSpec("Timestamp", ArgumentSpec.reference("param:welder:NozzleProduction.Timestamp"));
		descriptor.addInputArgumentSpec("NozzleProduction", ArgumentSpec.reference("param:welder:NozzleProduction"));

		descriptor.addOutputArgumentSpec("TotalThroughput", ArgumentSpec.reference("param:welder:TotalThroughput"));
		
		return descriptor;
	}
}
