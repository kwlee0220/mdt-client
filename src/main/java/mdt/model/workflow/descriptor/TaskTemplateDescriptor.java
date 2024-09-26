package mdt.model.workflow.descriptor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.func.Funcs;

import mdt.model.AASUtils;
import mdt.model.workflow.descriptor.port.PortDescriptor;
import mdt.model.workflow.descriptor.port.PortType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class TaskTemplateDescriptor {
	@JsonProperty("id") private String m_id;
	@JsonProperty("name") @Nullable private String m_name;
	@JsonProperty("type") private String m_type;
	@JsonProperty("description") @Nullable private String m_description;
	@JsonProperty("parameters") private KeyedValueList<String,ParameterDescriptor> m_parameters;
	@JsonProperty("inputPorts") private KeyedValueList<String,PortDescriptor> m_inputPorts;
	@JsonProperty("outputPorts") private KeyedValueList<String,PortDescriptor> m_outputPorts;
	@JsonProperty("options") private KeyedValueList<String,OptionDescriptor> m_options;
	
	public TaskTemplateDescriptor() {
		m_parameters = new KeyedValueList<>(ParameterDescriptor::getName);
		m_inputPorts = new KeyedValueList<>(PortDescriptor::getName);
		m_outputPorts = new KeyedValueList<>(PortDescriptor::getName);
		m_options = new KeyedValueList<>(OptionDescriptor::getName);
	}
	
	private TaskTemplateDescriptor(Builder builder) {
		m_id = builder.m_id;
		m_name = builder.m_name;
		m_type = builder.m_type;
		m_description = builder.m_description;
		
		setParameters(builder.m_parameters);
		setInputPorts(builder.m_inputPorts);
		setOutputPorts(builder.m_outputPorts);
		setOptions(builder.m_options);
	}
	
	public String getId() {
		return m_id;
	}
	public void setId(String id) {
		m_id = id;
	}

	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}

	public String getType() {
		return m_type;
	}
	public void setType(String type) {
		Preconditions.checkArgument(type != null);
		m_type = type;
	}

	public String getDescription() {
		return m_description;
	}
	public void setDescription(String desc) {
		m_description = desc;
	}

	public KeyedValueList<String,ParameterDescriptor> getParameters() {
		return m_parameters;
	}
	public void setParameters(Collection<ParameterDescriptor> parameters) {
		m_parameters = (parameters != null)
					? KeyedValueList.from(parameters, ParameterDescriptor::getName)
					: new KeyedValueList<>(ParameterDescriptor::getName);
	}

	public KeyedValueList<String, PortDescriptor> getInputPorts() {
		return m_inputPorts;
	}
	public List<PortDescriptor> findInputPortsOfType(PortType type) {
		return Funcs.filter(m_inputPorts, p -> p.getPortType().equals(type));
	}
	public void setInputPorts(Collection<PortDescriptor> inputPorts) {
		m_inputPorts = (inputPorts != null)
						? KeyedValueList.from(inputPorts, PortDescriptor::getName)
						: new KeyedValueList<>(PortDescriptor::getName);
	}

	public KeyedValueList<String, PortDescriptor> getOutputPorts() {
		return m_outputPorts;
	}
	public List<PortDescriptor> findOutputPortsOfType(PortType type) {
		return Funcs.filter(m_outputPorts, p -> p.getPortType().equals(type));
	}
	public void setOutputPorts(Collection<PortDescriptor> outputPorts) {
		m_outputPorts = (outputPorts != null)
						? KeyedValueList.from(outputPorts, PortDescriptor::getName)
						: new KeyedValueList<>(PortDescriptor::getName);
	}

	public KeyedValueList<String, OptionDescriptor> getOptions() {
		return m_options;
	}
	public void setOptions(Collection<OptionDescriptor> options) {
		m_options = (options != null)
					? KeyedValueList.from(options, OptionDescriptor::getName)
					: new KeyedValueList<>(OptionDescriptor::getName);
	}
	
	public static TaskTemplateDescriptor parseJsonFile(File jsonFile) throws IOException {
		return AASUtils.getJsonMapper().readValue(jsonFile, TaskTemplateDescriptor.class);
	}
	
	public static List<TaskTemplateDescriptor> parseListJsonFile(File jsonFile) throws IOException {
		return AASUtils.getJsonMapper().readValue(jsonFile,
													new TypeReference<List<TaskTemplateDescriptor>>() {});
	}
	
	public String toJsonString() {
		return AASUtils.writeJson(this);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		TaskTemplateDescriptor other = (TaskTemplateDescriptor)obj;
		return Objects.equal(m_id, other.m_id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_id);
	}
	
	@Override
	public String toString() {
		return String.format("TaskTemplate(id=%s, type=%s)", m_id, m_type);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_id;
		private String m_name;
		private String m_type;
		private String m_description;
		private Collection<ParameterDescriptor> m_parameters;
		private Collection<PortDescriptor> m_inputPorts;
		private Collection<PortDescriptor> m_outputPorts;
		private Collection<OptionDescriptor> m_options;
		
		public TaskTemplateDescriptor build() {
			return new TaskTemplateDescriptor(this);
		}
		
		public Builder id(String id) {
			m_id = id;
			return this;
		}
		
		public Builder name(String name) {
			m_name = name;
			return this;
		}
		
		public Builder type(String type) {
			m_type = type;
			return this;
		}
		
		public Builder description(String description) {
			m_description = description;
			return this;
		}
		
		public Builder parameters(Collection<ParameterDescriptor> parameters) {
			m_parameters = parameters;
			return this;
		}
		
		public Builder inputPorts(Collection<PortDescriptor> inputPorts) {
			m_inputPorts = inputPorts;
			return this;
		}
		
		public Builder outputPorts(Collection<PortDescriptor> outputPorts) {
			m_outputPorts = outputPorts;
			return this;
		}
		
		public Builder options(Collection<OptionDescriptor> options) {
			m_options = options;
			return this;
		}
	}
}
