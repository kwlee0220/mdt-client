package mdt.test;

import java.io.File;
import java.io.IOException;

import mdt.workflow.model.WorkflowDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestWorkflowDescriptor {
	public static final void main(String... args) throws Exception {
		loadWfDesc(new File("misc/test-workflows/workflow.json"));
		
//		HttpMDTManagerClient manager = HttpMDTManagerClient.connect("http://localhost:12985");
//		HttpWorkflowManagerClient wfManager = manager.getWorkflowManager();
		
//		List<WorkflowDescriptor> descList = wfManager.getWorkflowDescriptorAll();
//		System.out.println(descList);
//		
//		JsonMapper mapper = AASUtils.getJsonMapper();
//		WorkflowDescriptor desc = mapper.readValue(new File("misc/test-workflows/workflow_0.json"),
//													WorkflowDescriptor.class);
//		System.out.println(desc);
//		
//		mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, desc);
	}
	
	public static void loadWfDesc(File file) throws IOException {
		WorkflowDescriptor wfDesc = WorkflowDescriptor.parseJsonFile(file);
//		System.out.println(wfDesc);
		
		String json = wfDesc.toJsonString();
		System.out.println(json);
		wfDesc = WorkflowDescriptor.parseJsonString(json);
	}
}
