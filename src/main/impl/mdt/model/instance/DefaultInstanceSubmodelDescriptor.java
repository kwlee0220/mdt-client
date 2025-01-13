package mdt.model.instance;

import com.google.common.base.Preconditions;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultInstanceSubmodelDescriptor implements InstanceSubmodelDescriptor {
	private final String m_id;
	private final String m_idShort;
	private final String m_semanticId;
	
	public DefaultInstanceSubmodelDescriptor(String id, String idShort, String semanticId) {
		Preconditions.checkArgument(id != null, "null id");
		Preconditions.checkArgument(idShort != null, "null idShort");
		Preconditions.checkArgument(semanticId != null, "null semanticId");
		
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
}
