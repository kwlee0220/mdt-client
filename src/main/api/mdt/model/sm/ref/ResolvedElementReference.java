package mdt.model.sm.ref;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({ "instanceId", "submodelId", "elementPath", "requestUrl" })
public final class ResolvedElementReference {
	private final String m_instanceId;
	private final String m_submodelId;
	private final String m_elementPath;
	private final String m_requestUrl;
	
	@JsonCreator
	public ResolvedElementReference(@JsonProperty("instanceId") String instanceId,
									@JsonProperty("submodelId") String submodelId,
									@JsonProperty("elementPath") String elementPath,
									@JsonProperty("requestUrl") String requestUrl) {
		m_instanceId = instanceId;
		m_submodelId = submodelId;
		m_elementPath = elementPath;
		m_requestUrl = requestUrl;
	}
	
	public String getInstanceId() {
		return m_instanceId;
	}
	
	public String getSubmodelId() {
		return m_submodelId;
	}

	public String getElementPath() {
		return m_elementPath;
	}
	
	public String getRequestUrl() {
		return m_requestUrl;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof ResolvedElementReference) ) {
			return false;
		}

		ResolvedElementReference other = (ResolvedElementReference) obj;
		return m_instanceId.equals(other.m_instanceId) && m_submodelId.equals(other.m_submodelId)
				&& m_elementPath.equals(other.m_elementPath);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_instanceId, m_submodelId, m_elementPath);
	}
	
	@Override
	public String toString() {
		return String.format("ResolvedElementReference [instanceId=%s, submodelId=%s, elementPath=%s, requestUrl=%s]",
				m_instanceId, m_submodelId, m_elementPath, m_requestUrl);
	}
}
