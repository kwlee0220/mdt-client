package mdt.model.sm;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum SubmodelElementReferenceType {
	DEFAULT,
	PARAMETER,
	OPERATION_VARIABLE,
	FILE,
	LITERAL,
	STDOUT;
	
	public static SubmodelElementReferenceType fromName(String name) {
		for ( SubmodelElementReferenceType refType: values() ) {
			if ( refType.name().equalsIgnoreCase(name) ) {
				return refType;
			}
		}
		for ( SubmodelElementReferenceType refType: values() ) {
			if ( refType.name().toLowerCase().startsWith(name.toLowerCase()) ) {
				return refType;
			}
		}
		if ( name.toLowerCase().startsWith("opv") ) {
			return SubmodelElementReferenceType.OPERATION_VARIABLE;
		}
		
		throw new IllegalArgumentException("Invalid SubmodelElementReferenceType type=" + name);
	}
}