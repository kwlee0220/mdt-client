package mdt.sample;

import java.util.List;

import mdt.client.workflow.HttpWorkflowManagerProxy;
import mdt.workflow.model.WorkflowDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleListWorkflowDescriptors {
	private static final String ENDPOINT = "http://localhost:12985/workflow-manager";
	
	public static final void main(String... args) throws Exception {
		HttpWorkflowManagerProxy manager = HttpWorkflowManagerProxy.builder()
																	.endpoint(ENDPOINT)
																	.build();
		
		List<WorkflowDescriptor> wfDescList = manager.getWorkflowDescriptorAll();
		for ( WorkflowDescriptor inst: wfDescList ) {
			System.out.println(inst);
		}
	}
}
