package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"id", "idShort", "semanticId", "endpoint"})
public final class MDTSubmodelDescriptor {
	private final String m_id;
	private final String m_idShort;
	private final String m_semanticId;
	private String m_endpoint;
	
	public MDTSubmodelDescriptor(@JsonProperty("id") String id,
								@JsonProperty("idShort") String idShort,
								@Nullable @JsonProperty("semanticId") String semanticId) {
		Preconditions.checkArgument(id != null, "null id");
		Preconditions.checkArgument(idShort != null, "null idShort");
		
		m_id = id;
		m_idShort = idShort;
		m_semanticId = semanticId;
		m_endpoint = null;
	}

	/**
	 * Gets the unique identifier of the instance submodel descriptor.
	 *
	 * @return the unique identifier as a String.
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * Gets the idShort of the instance submodel descriptor.
	 *
	 * @return the idShort as a String.
	 */
	public String getIdShort() {
		return m_idShort;
	}

	/**
	 * Gets the semantic identifier of the instance submodel descriptor.
	 *
	 * @return the semantic identifier as a String.
	 */
	public String getSemanticId() {
		return m_semanticId;
	}
	
	public String getEndpoint() {
		return m_endpoint;
	}
	
	public void setEndpoint(String endpoint) {
		m_endpoint = endpoint;
	}
	
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
            return false;
		}
		
		MDTSubmodelDescriptor other = (MDTSubmodelDescriptor)obj;
		return m_id.equals(other.m_id);
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s): %s", getClass().getSimpleName(), m_idShort, m_endpoint);
	}
}
