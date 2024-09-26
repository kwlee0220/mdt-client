package mdt.model.workflow.argo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.common.collect.Lists;

import utils.InternalException;

import mdt.model.NameValue;
import mdt.model.workflow.descriptor.ParameterDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
public class ArgoWorkflowDescriptor {
	private final String apiVersion = "argoproj.io/v1alpha1";
	private final String kind = "Workflow";
	private final Metadata metadata;
	private final Spec spec;
	
	@JsonIgnore
	private final Map<String,String> arguments;

	public ArgoWorkflowDescriptor(WorkflowDescriptor wfDesc, String mdtEndpoint, Map<String,String> arguments) {
		this.arguments = arguments;
		this.metadata = new Metadata(wfDesc.getId().toLowerCase() + "-");
		
		ArgoTemplateDescriptorLoader loader = new ArgoTemplateDescriptorLoader(wfDesc, mdtEndpoint);
		List<ArgoTemplateDescriptor> templates = loader.load();
		this.spec = new Spec(wfDesc, templates);
	}
	
	@Getter
	public static class Metadata {
		private final String generateName;
		
		public Metadata(@JsonProperty("generateName") String generateName) {
			this.generateName = generateName;
		}
	}
	
	@Getter
	public class Spec {
		@NonNull private final String entrypoint;
		private ArgoArgumentsDescriptor arguments;
		@NonNull private final List<ArgoTemplateDescriptor> templates;
		
		public Spec(WorkflowDescriptor wfDesc, List<ArgoTemplateDescriptor> templates) {
			this.entrypoint = "dag";
			this.templates = templates;
			
			List<NameValue> parameters = Lists.newArrayList();
			for ( ParameterDescriptor pdesc: wfDesc.getParameters() ) {
				String argName = pdesc.getName();
				String argValue = ArgoWorkflowDescriptor.this.arguments.get(pdesc.getName());
				if ( argValue == null ) {
					argValue = pdesc.getValue();
					if ( argValue == null ) {
						throw new IllegalStateException("Undefined workflow-parameter: name=" + argName);
					}
				}
				parameters.add(NameValue.of(argName, argValue));
			}
			if ( parameters.size() > 0 ) {
				this.arguments = new ArgoArgumentsDescriptor(parameters);
			}
		}
	}
	
	public String toYamlString(boolean prettyPrint) throws JsonProcessingException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
			toYaml(baos, prettyPrint);
			return baos.toString();
		}
		catch ( JsonProcessingException e ) {
			throw e;
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}
	
	public void toYaml(OutputStream os, boolean prettyPrint) throws IOException {
		try ( BufferedOutputStream bos = new BufferedOutputStream(os) ) {
			YAMLFactory yamlFact = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
													.enable(Feature.MINIMIZE_QUOTES);
			ObjectMapper mapper = new ObjectMapper(yamlFact);
			if ( prettyPrint ) {
				mapper.writerWithDefaultPrettyPrinter()
						.writeValue(bos, this);
			}
			else {
				mapper.writeValue(bos, this);
			}
		}
	}
}
