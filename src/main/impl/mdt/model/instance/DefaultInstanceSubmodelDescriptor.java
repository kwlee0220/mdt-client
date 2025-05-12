package mdt.model.instance;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"id", "idShort", "semanticId"})
public final class DefaultInstanceSubmodelDescriptor implements InstanceSubmodelDescriptor {
	private final String m_id;
	private final String m_idShort;
	private final String m_semanticId;
	
	public DefaultInstanceSubmodelDescriptor(@JsonProperty("id") String id,
												@JsonProperty("idShort") String idShort,
												@Nullable @JsonProperty("semanticId") String semanticId) {
		Preconditions.checkArgument(id != null, "null id");
		Preconditions.checkArgument(idShort != null, "null idShort");
		
		m_id = id;
		m_idShort = idShort;
		m_semanticId = semanticId;
	}
	
	public String getId() {
		return m_id;
	}

	public String getIdShort() {
		return m_idShort;
	}

	public String getSemanticId() {
		return m_semanticId;
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
		
		DefaultInstanceSubmodelDescriptor other = (DefaultInstanceSubmodelDescriptor)obj;
		return m_id.equals(other.m_id);
	}
	
	@Override
	public String toString() {
		return String.format("InstanceSubmodelDescriptor(idShort=%s)", m_idShort);
	}
}
