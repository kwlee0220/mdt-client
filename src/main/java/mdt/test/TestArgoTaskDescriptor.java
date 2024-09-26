package mdt.test;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import mdt.model.workflow.argo.ArgoWorkflowDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestArgoTaskDescriptor {
	public static final void main(String... args) throws Exception {
		WorkflowDescriptor wfDesc = WorkflowDescriptor.parseJsonFile(new File("misc/test-workflows/workflow.json"));

//		Map<String,String> arguments = Map.of("simulation-twin", "QC",
//											"simulation-submodel", "Simulation");
		Map<String,String> arguments = Map.of("simulation-twin", "QC",
											"simulation-submodel", "Simulation",
											"simulation-server-endpoint",
											"http://129.254.91.75:12987/operations/test-operation");
		ArgoWorkflowDescriptor argoWfDesc = new ArgoWorkflowDescriptor(wfDesc,
																"http://129.254.91.75:12985",
																arguments);
		YAMLFactory yamlFact = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
												.enable(Feature.MINIMIZE_QUOTES);
		ObjectMapper mapper = new ObjectMapper(yamlFact);
		String script = mapper.writeValueAsString(argoWfDesc);
		System.out.println(script);
	}
}