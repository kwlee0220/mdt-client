package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import mdt.model.sm.value.NamedValueType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"name", "operationType", "inputArguments", "outputArguments"})
public class DefaultMDTOperationDescriptor implements MDTOperationDescriptor {
	private final String m_name;
	private final String m_operationType;
	private final List<NamedValueType> m_inputArguments;
	private final List<NamedValueType> m_outputArguments;
	
	public DefaultMDTOperationDescriptor(@JsonProperty("name") String name,
											@JsonProperty("operationType") String type,
											@JsonProperty("inputArguments") List<NamedValueType> inputArguments,
											@JsonProperty("outputArguments") List<NamedValueType> outputArguments) {
		Preconditions.checkArgument(name != null, "null id");
//		Preconditions.checkArgument(type != null, "null type");
		Preconditions.checkArgument(inputArguments != null, "null inputArguments");
		Preconditions.checkArgument(outputArguments != null, "null outputArguments");
		
		m_name = name;
		m_operationType = type;
		m_inputArguments = inputArguments;
		m_outputArguments = outputArguments;
	}
	
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getOperationType() {
		return m_operationType;
	}

	@Override
	public List<NamedValueType> getInputArguments() {
		return m_inputArguments;
	}

	@Override
	public List<NamedValueType> getOutputArguments() {
		return m_outputArguments;
	}
}
