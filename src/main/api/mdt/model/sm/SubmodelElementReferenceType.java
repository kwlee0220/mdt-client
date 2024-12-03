package mdt.model.sm;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum SubmodelElementReferenceType {
	DEFAULT("default"),
	PARAMETER("parameter"),
	OPERATION_VARIABLE("opvar"),
	ARGUMENT("argument"),
	IN_MEMORY("memory"),
	FILE("file"),
	LITERAL("literal"),
	STDOUT("stdout");
	
	private final String m_code;
	
	SubmodelElementReferenceType(String code) {
		m_code = code;
	}
	
	public static SubmodelElementReferenceType fromName(String name) {
		name = name.toLowerCase();
		for ( SubmodelElementReferenceType refType: values() ) {
			if ( refType.m_code.startsWith(name) ) {
				return refType;
			}
		}
		
		throw new IllegalArgumentException("Invalid SubmodelElementReferenceType type=" + name);
	}
}