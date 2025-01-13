package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.KeyedValueList;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class DefaultOperationInfo implements OperationInfo {
	@JsonProperty("id") private final String m_id;
	@JsonProperty("type") private final String m_type;
	@JsonProperty("inputArguments") private final KeyedValueList<String, ParameterInfo> m_inputArguments;
	@JsonProperty("outputArguments") private final KeyedValueList<String, ParameterInfo> m_outputArguments;

	@JsonCreator
	public DefaultOperationInfo(@JsonProperty("id") String id, @JsonProperty("type") String kind,
			                        @JsonProperty("inputArguments") List<ParameterInfo> inputArgs,
                                    @JsonProperty("outputArguments") List<ParameterInfo> outputArgs) {
		m_id = id;
		m_type = kind;
		m_inputArguments = KeyedValueList.from(inputArgs, ParameterInfo::getId);
		m_outputArguments = KeyedValueList.from(outputArgs, ParameterInfo::getId);
	}
	
	@Override
	public String getId() {
		return m_id;
	}
	@Override
	public String getType() {
		return m_type;
	}

	@Override
	public KeyedValueList<String, ParameterInfo> getInputArguments() {
		return m_inputArguments;
	}

	@Override
	public KeyedValueList<String, ParameterInfo> getOutputArguments() {
		return m_outputArguments;
	}
	
	@Override
	public String toString() {
		String inArgs = FStream.from(this.m_inputArguments)
								.map(pinfo -> String.format("%s:%s", pinfo.getId(), pinfo.getType()))
								.join(", ");
		String outArgs = FStream.from(this.m_outputArguments)
								.map(pinfo -> String.format("%s:%s", pinfo.getId(), pinfo.getType()))
								.join(", ");
		
		return String.format("[%s] %s:(%s) -> {%s}", this.m_type, this.m_id, inArgs, outArgs);
	}
}
