package mdt.sample;

import java.util.Collection;
import java.util.List;

import mdt.client.workflow.HttpWorkflowManagerClient;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleListWorkflowDescriptors {
	private static final String ENDPOINT = "http://localhost:12985/workflow-manager";
	
	public static final void main(String... args) throws Exception {
		HttpWorkflowManagerClient manager = HttpWorkflowManagerClient.builder()
																	.endpoint(ENDPOINT)
																	.build();
		
		Collection<TaskTemplateDescriptor> taskDescList = manager.getBuiltInTaskTemplateAll();
		for ( TaskTemplateDescriptor tmplt: taskDescList ) {
			System.out.println(tmplt);
		}
		
		List<WorkflowDescriptor> wfDescList = manager.getWorkflowDescriptorAll();
		for ( WorkflowDescriptor inst: wfDescList ) {
			System.out.println(inst);
		}
	}
}
