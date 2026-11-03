package mdt.model.timeseries;

import mdt.model.sm.entity.PropertyField;


/**
 * {@link LinkedSegment}의 기본 구현 클래스.
 * <p>
 * endpoint와 질의문은 각각 {@code Endpoint}, {@code Query} idShort의 Property와 매핑된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultLinkedSegment extends DefaultSegment implements LinkedSegment {
	@PropertyField(idShort="Endpoint") private String endpoint;
	@PropertyField(idShort="Query") private String query;
	
	/**
	 * 빈 LinkedSegment 객체를 생성한다.
	 * <p>
	 * semanticId는 {@link LinkedSegment#SEMANTIC_ID}로 설정된다.
	 */
	public DefaultLinkedSegment() {
		setSemanticId(LinkedSegment.SEMANTIC_ID);
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	
	/**
	 * 레코드들이 저장된 외부 저장소의 endpoint를 설정한다.
	 *
	 * @param endpoint	외부 저장소의 endpoint.
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public String getQuery() {
		return query;
	}
	
	/**
	 * 외부 저장소에서 레코드들을 조회하기 위한 질의문을 설정한다.
	 *
	 * @param query	질의문.
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	public String toString() {
		return "LinkedSegment[id=" + getIdShort() + ", endpoint=" + getEndpoint() + ", query=" + getQuery() + "]";
	}
}
