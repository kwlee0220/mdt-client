package mdt.workflow.model.port;

import lombok.experimental.UtilityClass;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PortDescriptors {
	public static PortDescriptor parseStringExpr(String name, String desc, String expr) {
		if ( expr.equals(PortType.STDOUT.getId()) ) {
			return new StdoutPortDescriptor(name, desc);
		}
		
		String[] parts = expr.split(":");
		if ( parts.length > 1 ) {
			String valueExpr = expr.substring(parts[0].length() + 1);
			
			PortType ptype = PortType.fromId(parts[0]);
			return switch ( ptype ) {
				case SME -> SubmodelElementPortDescriptor.parseStringExpr(name, desc, valueExpr);
				case PARAMETER -> MDTParameterPortDescriptor.parseString(name, desc, valueExpr);
				case FILE -> FilePortDescriptor.parseStringExpr(name, desc, valueExpr);
				case VARIABLE -> WorkflowVariablePortDescriptor.parseStringExpr(name, desc, valueExpr);
				case LITERAL -> LiteralPortDescriptor.parseStringExpr(name, desc, valueExpr);
				default -> throw new IllegalArgumentException("Invalid PortDescriptor expression: " + expr);
			};
		}
		else {
			return SubmodelElementPortDescriptor.parseStringExpr(name, desc, expr);
		}
	}
}
