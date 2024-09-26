package mdt.model.workflow.descriptor.port;

import lombok.experimental.UtilityClass;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PortDescriptors {
	public static final String VALUE_ONLY_MARKER = "*";

	public static PortDescriptor parseStringExpr(String name, String desc, String expr) {
		boolean valueOnly = expr.startsWith(VALUE_ONLY_MARKER);
		if ( valueOnly ) {
			expr = expr.substring(1);
		}
		
		if ( expr.equals(PortType.STDOUT.getId()) ) {
			return new StdoutPortDescriptor(name, desc, valueOnly);
		}
		
		String[] parts = expr.split(":");
		if ( parts.length > 1 ) {
			String valueExpr = expr.substring(parts[0].length() + 1);
			
			PortType ptype = PortType.fromId(parts[0]);
			return switch ( ptype ) {
				case SME -> SubmodelElementPortDescriptor.parseStringExpr(name, desc, valueExpr, valueOnly);
				case PARAMETER -> MDTParameterPortDescriptor.parseStringExpr(name, desc, valueExpr, valueOnly);
				case FILE -> FilePortDescriptor.parseStringExpr(name, desc, valueExpr, valueOnly);
				case VARIABLE -> WorkflowVariablePortDescriptor.parseStringExpr(name, desc, valueExpr, valueOnly);
				case LITERAL -> LiteralPortDescriptor.parseStringExpr(name, desc, valueExpr, valueOnly);
				default -> throw new IllegalArgumentException("Invalid PortDescriptor expression: " + expr);
			};
		}
		else {
			return SubmodelElementPortDescriptor.parseStringExpr(name, desc, expr, valueOnly);
		}
	}
}
