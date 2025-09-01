package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;


/**
 * MDT 파라미터의 인터페이스를 정의한다.
 * <p>
 * MDT 파라미터는 MDT 모델의 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "name", "valueType", "reference"})
public class MDTParameterDescriptor {
	private final String m_id;
	private final String m_name;
	private final String m_valueType;
	private final String m_reference;
	
	public MDTParameterDescriptor(@JsonProperty("id") String id,
									@JsonProperty("name") String name,
									@JsonProperty("valueType") String valueType,
									@JsonProperty("reference") String reference) {
		Preconditions.checkArgument(id != null, "Parameter id is null");
		Preconditions.checkArgument(valueType != null, "null type");
		
		m_id = id;
		m_name = name;
		m_valueType = valueType;
		m_reference = reference;
	}

	/**
	 * MDT 파라미터의 식별자를 반환한다.
	 * 
	 * @return	파라미터 식별자
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * MDT 파라미터의 이름을 반환한다.
	 * 
	 * @return	파라미터 이름
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * MDT 파라미터의 타입을 반환한다.
	 *
	 * @return 파라미터 타입
	 */
	public String getValueType() {
		return m_valueType;
	}

	/**
	 * MDT 파라미터의 참조 문자열을 반환한다.
	 *
	 * @return 참조 문자열
	 */
	public String getReference() {	
		return m_reference;
	}
}
