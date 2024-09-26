package mdt.task;

import lombok.experimental.UtilityClass;
import mdt.ksx9101.model.ParameterValueReference;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.workflow.descriptor.port.PortDirection;
import mdt.model.workflow.descriptor.port.PortType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Ports {
	private static final String VALUE_PREFIX = "*";
	
	public static Port from(MDTInstanceManager manager, String argName, String argValue) {
		int idx = argName.indexOf('.');
		if ( idx < 0 ) {
			String msg = String.format("invalid Port specification: '%s'", argName);
			throw new IllegalArgumentException(msg);
		}
		
		boolean valueOnly = argValue.startsWith(VALUE_PREFIX);
		if ( valueOnly ) {
			argValue = argValue.substring(1);
		}
		
		String pname = argName.substring(idx+1);
		PortDirection pdir = switch ( argName.substring(0, idx) ) {
			case "in" -> PortDirection.INPUT;
			case "out" -> PortDirection.OUTPUT;
			default -> throw new IllegalArgumentException(String.format("invalid Port specification: '%s'",
																		argName));
		};
		
		if ( argValue.equals(PortType.STDOUT.getId()) ) {
			return new StdOutPort(pname, valueOnly);
		}
		
		PortType ptype = PortType.SME;
		String refString = argValue;
		int delimIndex = argValue.indexOf(':');
		if ( delimIndex >= 0 ) {
			ptype = PortType.fromId(argValue.substring(0, delimIndex));
			refString = argValue.substring(delimIndex+1);
		}
		
		switch ( ptype ) {
			case SME:
				SubmodelElementReference smeRef = SubmodelElementReference.parseString(manager, refString);
				return new SubmodelElementPort(pname, pdir, smeRef, valueOnly);
			case PARAMETER:
				ParameterValueReference pvRef = ParameterValueReference.parseString(manager, refString);
				return new MDTParameterValuePort(pname, pdir, pvRef, valueOnly);
			case FILE:
				return new FilePort(pname, pdir, valueOnly, refString);
			case LITERAL:
				return new LiteralPort(pname, pdir, valueOnly, refString);
			default:
				throw new IllegalArgumentException("Unsupported port type: " + ptype);
		}
	}
	
	public static void printPort(Port port, String value) {
		System.out.printf("[%s] %s:%n", port.getDirection() == PortDirection.INPUT ? "IN":"OUT", port.getName());
		System.out.println(value);
		System.out.println("------------------------------------------------------------------------");
	}
	
	public static void printOutputPort(String name, String value) {
		System.out.printf("[%s] %s:%n", "OUT", name);
		System.out.println(value);
		System.out.println("------------------------------------------------------------------------");
	}
}
