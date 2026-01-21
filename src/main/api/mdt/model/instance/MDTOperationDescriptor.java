package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;

import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "operationType", "inputArguments", "outputArguments"})
public class MDTOperationDescriptor {
	private final String m_id;
	private final String m_operationType;
	private final List<ArgumentDescriptor> m_inputArguments;
	private final List<ArgumentDescriptor> m_outputArguments;
	
	public MDTOperationDescriptor(@JsonProperty("id") String id,
									@JsonProperty("operationType") String type,
									@JsonProperty("inputArguments") List<ArgumentDescriptor> inArgs,
									@JsonProperty("outputArguments") List<ArgumentDescriptor> outArgs) {
		Preconditions.checkArgument(id != null, "Submodel idShort is null");
		Preconditions.checkArgument(type != null, "Operation type is null");
		Preconditions.checkArgument(inArgs != null, "null inputArguments");
		Preconditions.checkArgument(outArgs != null, "null outputArguments");
		
		m_id = id;
		m_operationType = type;
		m_inputArguments = inArgs;
		m_outputArguments = outArgs;
	}

	/**
	 * MDT 연산의 식별자을 반환한다.
	 * 
	 * @return	MDT 연산 식별자
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * MDT 연산의 타입을 반환한다.
	 *
	 * @return MDT 연산 타입
	 */
	public String getOperationType() {
		return m_operationType;
	}
	
	/**
	 * MDT 연산의 입력 인자 목록을 반환한다.
	 *
	 * @return	입력 파라미터 목록
	 */
	public List<ArgumentDescriptor> getInputArguments() {
		return m_inputArguments;
	}
	
	/**
	 * MDT 연산의 출력 인자 목록을 반환한다.
	 *
	 * @return	출력 인자 목록
	 */
	public List<ArgumentDescriptor> getOutputArguments() {
		return m_outputArguments;
	}
	
	public String toSignatureString() {
		String inArgsStr = FStream.from(getInputArguments())
									.map(ArgumentDescriptor::getId)
									.join(", ");
		String outArgsStr = FStream.from(getOutputArguments())
									.map(ArgumentDescriptor::getId)
									.join(", ");
		return String.format("%s(%s) -> (%s)", getId(), inArgsStr, outArgsStr);
	}
	
	@Override
	public String toString() {
		String inArgList = FStream.from(m_inputArguments).map(arg -> arg.getId()).join(", ");
		String outArgList = FStream.from(m_outputArguments).map(arg -> arg.getId()).join(", ");
		return String.format("[%s] %s(%s) -> (%s)", m_operationType, m_id, inArgList, outArgList);
	}
	

	@JsonPropertyOrder({"id", "idShortPath", "valueType", "reference"})
	public static class ArgumentDescriptor {
		private final String m_id;
		private final String m_idShortPath;
		private final String m_valueType;
		private final String m_reference;
		
		public ArgumentDescriptor(@JsonProperty("id") String id,
									@JsonProperty("idShortPath") String idShortPath,
									@JsonProperty("valueType") String valueType,
									@JsonProperty("reference") String reference) {
			m_id = id;
			m_valueType = valueType;
			m_reference = reference;
			m_idShortPath = idShortPath;
		}
		
		public String getId() {
			return m_id;
		}
		
		public String getValueType() {
			return m_valueType;
		}
		
		public String getReference() {
			return m_reference;
		}

		public String getIdShortPath() {
			return m_idShortPath;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", m_id, m_valueType);
		}
	}
}
