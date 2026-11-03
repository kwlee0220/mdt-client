package mdt.model.instance;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.NoArgsConstructor;

import utils.Preconditions;


/**
 * MDT 파라미터 기술자를 정의한다.
 * <p>
 * MDT 파라미터는 MDT 모델의 구성요소로서, 식별자, 이름, 값 타입, 참조 문자열
 * (예: {@code param:<instanceId>:<paramId>}), 그리고 값에 접근하기 위한 RESTful endpoint 정보를 갖는다.
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
	
	/**
	 * MDT 파라미터 기술자를 생성한다.
	 * <p>
	 * endpoint는 설정되지 않은 상태({@code null})로 생성된다.
	 *
	 * @param id		파라미터 식별자.
	 * @param name		파라미터 이름.
	 * @param valueType	파라미터 값 타입.
	 * @param reference	파라미터 참조 문자열.
	 * @throws IllegalArgumentException	{@code id} 또는 {@code valueType}이 {@code null}인 경우.
	 */
	public MDTParameterDescriptor(String id, String name, String valueType, String reference) {
		Preconditions.checkNotNullArgument(id, "Parameter id is null");
		Preconditions.checkNotNullArgument(valueType, "null type");
		
		m_id = id;
		m_name = name;
		m_valueType = valueType;
		m_reference = reference;
	}
	
	/**
	 * 파라미터 식별자를 반환한다.
	 *
	 * @return	파라미터 식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * 파라미터 식별자를 설정한다.
	 *
	 * @param id	파라미터 식별자.
	 */
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * 파라미터 이름을 반환한다.
	 *
	 * @return	파라미터 이름.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * 파라미터 이름을 설정한다.
	 *
	 * @param name	파라미터 이름.
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * 파라미터 값 타입을 반환한다.
	 *
	 * @return	파라미터 값 타입.
	 */
	public String getValueType() {
		return m_valueType;
	}
	
	/**
	 * 파라미터 값 타입을 설정한다.
	 *
	 * @param valueType	파라미터 값 타입.
	 */
	public void setValueType(String valueType) {
		m_valueType = valueType;
	}
	
	/**
	 * 파라미터 참조 문자열을 반환한다.
	 *
	 * @return	파라미터 참조 문자열.
	 */
	public String getReference() {
		return m_reference;
	}
	
	/**
	 * 파라미터 참조 문자열을 설정한다.
	 *
	 * @param reference	파라미터 참조 문자열.
	 */
	public void setReference(String reference) {
		m_reference = reference;
	}
	
	/**
	 * 파라미터 값을 다루기 위한 RESTful endpoint를 반환한다.
	 *
	 * @return	endpoint URL. 설정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getEndpoint() {
		return m_endpoint;
	}
	
	/**
	 * 파라미터 값을 다루기 위한 RESTful endpoint를 설정한다.
	 *
	 * @param endpoint	endpoint URL.
	 */
	public void setEndpoint(@Nullable String endpoint) {
		m_endpoint = endpoint;
	}
}
