package mdt.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class MDTManagerEvent {
	private final String m_eventType;
	
	protected MDTManagerEvent(String eventType) {
		m_eventType = eventType;
	}
	
	@JsonProperty("eventType")
	public String getEventType() {
		return m_eventType;
	}
}
