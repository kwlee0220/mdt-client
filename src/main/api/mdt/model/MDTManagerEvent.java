package mdt.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * MDTManager가 발행하는 이벤트들의 기반 클래스이다.
 * <p>
 * 모든 이벤트는 이벤트 종류를 나타내는 {@code eventType} 속성을 가지며, JSON 직렬화 시
 * {@code null}인 속성은 생략된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class MDTManagerEvent {
	private final String m_eventType;
	
	/**
	 * 주어진 이벤트 종류로 이벤트 객체를 생성한다.
	 *
	 * @param eventType	이벤트 종류.
	 */
	protected MDTManagerEvent(String eventType) {
		m_eventType = eventType;
	}
	
	/**
	 * 이벤트 종류를 반환한다.
	 *
	 * @return	이벤트 종류.
	 */
	@JsonProperty("eventType")
	public String getEventType() {
		return m_eventType;
	}
}
