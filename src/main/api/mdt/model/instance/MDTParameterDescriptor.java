package mdt.model.instance;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.NoArgsConstructor;

import utils.Preconditions;


/**
 * MDT 파라미터 기술자를 정의한다.
 * <p>
 * MDT 파라미터는 MDT 모델의 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@JsonPropertyOrder({"id", "name", "valueType", "reference", "endpoint"})
public class MDTParameterDescriptor {
	/** 파라미터 식별자 */
	private String m_id;
	/** 파라미터 이름 */
	private String m_name;
	/** 파라미터 타입 */
	private String m_valueType;
	/** 파라미터 참조 문자열 */
	private String m_reference;
	/** 파라미터 값을 다루기 위한 RESTful 엔드포인트 */
	private @Nullable String m_endpoint;
	
	public MDTParameterDescriptor(String id, String name, String valueType, String reference) {
		Preconditions.checkNotNullArgument(id, "Parameter id is null");
		Preconditions.checkNotNullArgument(valueType, "null type");
		
		m_id = id;
		m_name = name;
		m_valueType = valueType;
		m_reference = reference;
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
	
	public String getValueType() {
		return m_valueType;
	}
	
	public void setValueType(String valueType) {
		m_valueType = valueType;
	}
	
	public String getReference() {
		return m_reference;
	}
	
	public void setReference(String reference) {
		m_reference = reference;
	}
	
	public @Nullable String getEndpoint() {
		return m_endpoint;
	}
	
	public void setEndpoint(@Nullable String endpoint) {
		m_endpoint = endpoint;
	}
}
