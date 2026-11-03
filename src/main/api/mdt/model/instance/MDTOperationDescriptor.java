package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.Preconditions;
import utils.stream.FStream;

/**
 * MDTInstance가 제공하는 MDT 연산(AI 또는 Simulation 연산)의 정보를 담는 디스크립터.
 * <p>
 * 연산은 해당 연산 Submodel의 idShort로 식별되며, 연산 타입과 입력/출력 인자 목록을 갖는다.
 * 각 인자는 {@link ArgumentDescriptor}로 표현된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "operationType", "inputArguments", "outputArguments"})
public class MDTOperationDescriptor {
	private final String m_id;
	private final String m_operationType;
	private final List<ArgumentDescriptor> m_inputArguments;
	private final List<ArgumentDescriptor> m_outputArguments;
	
	/**
	 * MDT 연산 디스크립터를 생성한다.
	 *
	 * @param id		연산 식별자 (연산 Submodel의 idShort).
	 * @param type		연산 타입 (예: {@code "AI"}, {@code "Simulation"}).
	 * @param inArgs	입력 인자 목록.
	 * @param outArgs	출력 인자 목록.
	 * @throws IllegalArgumentException	인자 중 하나라도 {@code null}인 경우.
	 */
	public MDTOperationDescriptor(@JsonProperty("id") String id,
									@JsonProperty("operationType") String type,
									@JsonProperty("inputArguments") List<ArgumentDescriptor> inArgs,
									@JsonProperty("outputArguments") List<ArgumentDescriptor> outArgs) {
		Preconditions.checkNotNullArgument(id, "Submodel idShort is null");
		Preconditions.checkNotNullArgument(type, "Operation type is null");
		Preconditions.checkNotNullArgument(inArgs, "null inputArguments");
		Preconditions.checkNotNullArgument(outArgs, "null outputArguments");
		
		m_id = id;
		m_operationType = type;
		m_inputArguments = inArgs;
		m_outputArguments = outArgs;
	}

	/**
	 * MDT 연산의 식별자를 반환한다.
	 * <p>
	 * 연산 식별자는 해당 연산 Submodel의 idShort이다.
	 * 
	 * @return	MDT 연산 식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * MDT 연산의 타입을 반환한다.
	 *
	 * @return	MDT 연산 타입 (예: {@code "AI"}, {@code "Simulation"}).
	 */
	public String getOperationType() {
		return m_operationType;
	}
	
	/**
	 * MDT 연산의 입력 인자 목록을 반환한다.
	 *
	 * @return	입력 인자 목록.
	 */
	public List<ArgumentDescriptor> getInputArguments() {
		return m_inputArguments;
	}
	
	/**
	 * MDT 연산의 출력 인자 목록을 반환한다.
	 *
	 * @return	출력 인자 목록.
	 */
	public List<ArgumentDescriptor> getOutputArguments() {
		return m_outputArguments;
	}
	
	/**
	 * 연산의 시그니처 문자열을 반환한다.
	 * <p>
	 * 형식은 {@code <id>(<입력 인자 id 목록>) -> (<출력 인자 id 목록>)}이다.
	 *
	 * @return	시그니처 문자열.
	 */
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
	

	/**
	 * MDT 연산의 입력 또는 출력 인자 하나의 정보를 담는 디스크립터.
	 */
	@JsonPropertyOrder({"id", "idShortPath", "valueType", "reference"})
	public static class ArgumentDescriptor {
		private final String m_id;
		private final String m_idShortPath;
		private final String m_valueType;
		private final String m_reference;
		
		/**
		 * 인자 디스크립터를 생성한다.
		 *
		 * @param id			인자 식별자.
		 * @param idShortPath	연산 Submodel 내에서 인자 값 SubmodelElement의 idShortPath.
		 * @param valueType		인자 값의 타입 문자열.
		 * @param reference		인자를 가리키는 참조 문자열.
		 */
		public ArgumentDescriptor(@JsonProperty("id") String id,
									@JsonProperty("idShortPath") String idShortPath,
									@JsonProperty("valueType") String valueType,
									@JsonProperty("reference") String reference) {
			m_id = id;
			m_valueType = valueType;
			m_reference = reference;
			m_idShortPath = idShortPath;
		}
		
		/**
		 * 인자 식별자를 반환한다.
		 *
		 * @return	인자 식별자.
		 */
		public String getId() {
			return m_id;
		}
		
		/**
		 * 인자 값의 타입 문자열을 반환한다.
		 *
		 * @return	값 타입 문자열.
		 */
		public String getValueType() {
			return m_valueType;
		}
		
		/**
		 * 인자를 가리키는 참조 문자열을 반환한다.
		 *
		 * @return	참조 문자열.
		 */
		public String getReference() {
			return m_reference;
		}

		/**
		 * 연산 Submodel 내에서 인자 값 SubmodelElement의 idShortPath를 반환한다.
		 *
		 * @return	idShortPath.
		 */
		public String getIdShortPath() {
			return m_idShortPath;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", m_id, m_valueType);
		}
	}
}
