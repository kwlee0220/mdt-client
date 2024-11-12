package mdt.aas;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum EndpointInterface {
	AAS("AAS"),
	SUBMODEL("SUBMODEL"),
	AAS_SERIALIZE("AAS-SERIALIZE"),
	AASX_FILE("AASX-FILE"),
	AAS_REGISTRY("AAS-REGISTRY"),
	SUBMODEL_REGISTRY("SUBMODEL-REGISTRY"),
	AAS_REPOSITORY("AAS-REPOSITORY"),
	SUBMODEL_REPOSITORY("SUBMODEL-REPOSITORY"),
	CD_REPOSITORY("CD-REPOSITORY"),
	AAS_DISCOVERY("AAS-DISCOVERY");
	
	private final String m_name;
	
	EndpointInterface(String name) {
		m_name = name;
	}
	
	public String getName() {
		return m_name;
	}
}