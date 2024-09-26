package mdt.model.workflow.argo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import utils.Named;
import utils.func.FOption;

import lombok.ToString;
import mdt.model.NameValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class ArgoContainerTemplateDescriptor implements ArgoTemplateDescriptor {
	private final String m_name;
	private final InputsDescriptor m_inputs;
	private final OutputsDescriptor m_outputs;
	private final ContainerDescriptor m_container;
	
	@JsonCreator
	public ArgoContainerTemplateDescriptor(@JsonProperty("name") String name,
											@JsonProperty("inputs") InputsDescriptor inputs,
											@JsonProperty("outputs") OutputsDescriptor outputs,
											@JsonProperty("container") ContainerDescriptor container) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(container != null);
		
		this.m_name = name;
		this.m_inputs = inputs;
		this.m_outputs = outputs;
		this.m_container = container;
	}
	
	public String getName() {
		return m_name;
	}

	public InputsDescriptor getInputs() {
		return m_inputs;
	}
	
	public OutputsDescriptor getOutputs() {
		return m_outputs;
	}

	public ContainerDescriptor getContainer() {
		return m_container;
	}

	@JsonProperty("inputs")
	private InputsDescriptor getInputsForJson() {
		return (m_inputs.size() > 0) ? m_inputs : null;
	}

	@JsonProperty("outputs")
	private OutputsDescriptor getOutputsForJson() {
		return (m_outputs.size() > 0) ? m_outputs : null;
	}

	@JsonInclude(Include.NON_NULL)
	public static class InputsDescriptor {
		private List<NameDescriptor> m_parameters;
		
		public int size() {
			return FOption.mapOrElse(m_parameters, l -> l.size(), 0);
		}

		public List<NameDescriptor> getParameters() {
			return m_parameters;
		}
		public void setParameters(List<NameDescriptor> parameters) {
			m_parameters = parameters;
		}
	}

	@JsonInclude(Include.NON_NULL)
	public static class OutputsDescriptor {
		private List<OutputParameterBinding> m_parameters;
		private List<NamePath> m_artifacts;
		
		public int size() {
			return FOption.mapOrElse(m_parameters, l -> l.size(), 0)
					+ FOption.mapOrElse(m_artifacts, l -> l.size(), 0);
		}

		public List<OutputParameterBinding> getParameters() {
			return m_parameters;
		}
		public void setParameters(List<OutputParameterBinding> parameters) {
			m_parameters = parameters;
		}

		public List<NamePath> getArtifacts() {
			return m_artifacts;
		}
		public void setArtifacts(List<NamePath> artifacts) {
			m_artifacts = artifacts;
		}
	}
	
	public static class NameDescriptor {
		private final String m_name;
		
		@JsonCreator
		public NameDescriptor(@JsonProperty("name") String name) {
			m_name = name;
		}

		public String getName() {
			return m_name;
		}
	}
	
	public static class NamePath implements Named {
		private final String m_name;
		private final String m_path;
		
		@JsonCreator
		public NamePath(@JsonProperty("name") String name,
						@JsonProperty("path") String path) {
			m_name = name;
			m_path = path;
		}

		public String getName() {
			return m_name;
		}
		
		public String getPath() {
			return m_path;
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	@ToString
	public static class ContainerDescriptor {
		private final String image;
		private final List<String> command;
		@ToString.Exclude private final List<String> args;
		@ToString.Exclude private final List<NameValue> env;

		@JsonCreator
		public ContainerDescriptor(@JsonProperty("image") String image,
									@JsonProperty("command") List<String> command,
									@JsonProperty("args") List<String> args,
									@JsonProperty("env") List<NameValue> env) {
			Preconditions.checkArgument(image != null);
			Preconditions.checkArgument(command != null);
			
			this.image = image;
			this.command = command;
			this.args = args;
			this.env = env;
		}

		public String getImage() {
			return image;
		}

		public List<String> getCommand() {
			return command;
		}

		public List<String> getArgs() {
			return args;
		}

		public List<NameValue> getEnv() {
			return env;
		}
	}
}
