package mdt.model.sm.ref;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum SubmodelReferenceType {
	DEFAULT("default");
	
	private final String m_code;
	
	SubmodelReferenceType(String code) {
		m_code = code;
	}
	
	public static SubmodelReferenceType fromName(String name) {
		name = name.toLowerCase();
		for ( SubmodelReferenceType refType: values() ) {
			if ( refType.m_code.startsWith(name) ) {
				return refType;
			}
		}
		
		throw new IllegalArgumentException("Invalid SubmodelReferenceType type=" + name);
	}
}
