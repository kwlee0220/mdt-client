package mdt.model.sm.ref;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum ElementReferenceType {
	DEFAULT("default"),
	PARAMETER("parameter"),
	OPERATION_VARIABLE("opvar"),
	ARGUMENT("argument"),
	IN_MEMORY("memory"),
	FILE("file"),
	LITERAL("literal"),
	STDOUT("stdout");
	
	private final String m_code;
	
	ElementReferenceType(String code) {
		m_code = code;
	}
	
	public String getCode() {
		return m_code;
	}
	
	public static ElementReferenceType fromName(String name) {
		name = name.toLowerCase();
		for ( ElementReferenceType refType: values() ) {
			if ( refType.m_code.startsWith(name) ) {
				return refType;
			}
		}
		
		throw new IllegalArgumentException("Invalid SubmodelElementReferenceType type=" + name);
	}
}