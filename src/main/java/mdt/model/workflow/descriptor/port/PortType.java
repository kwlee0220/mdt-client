package mdt.model.workflow.descriptor.port;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum PortType {
	SME("sme"),
	PARAMETER("parameter"),
	FILE("file"),
	VARIABLE("var"),
	LITERAL("literal"),
	STDOUT("stdout"),
	DECLARE("declare");
	
	private String m_id;
	
	private PortType(String id) {
		m_id = id;
	}
	
	public static PortType fromId(String id) {
		for ( PortType ptype: values() ) {
			if ( ptype.getId().equals(id) ) {
				return ptype;
			}
		}
		for ( PortType ptype: values() ) {
			if ( ptype.getId().startsWith(id) ) {
				return ptype;
			}
		}
		throw new IllegalArgumentException("Invalid PortType id=" + id);
	}
	
	public String getId() {
		return m_id;
	}
}
