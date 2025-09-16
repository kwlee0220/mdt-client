package mdt.model.instance;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * MDT 파라미터 기술자를 정의한다.
 * <p>
 * MDT 파라미터는 MDT 모델의 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@Accessors(prefix="m_")
@Getter @Setter
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
		Preconditions.checkArgument(id != null, "Parameter id is null");
		Preconditions.checkArgument(valueType != null, "null type");
		
		m_id = id;
		m_name = name;
		m_valueType = valueType;
		m_reference = reference;
	}
}
