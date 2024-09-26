package mdt.model.workflow.descriptor.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType", "valueOnly"})
public class StdoutPortDescriptor extends AbstractPortDescriptor {
	public StdoutPortDescriptor() { }
	public StdoutPortDescriptor(String name, String description, boolean valueOnly) {
		super(name, description, PortType.STDOUT, valueOnly);
	}

	public static StdoutPortDescriptor parseStringExpr(String name, String description, boolean valueOnly) {
		return new StdoutPortDescriptor(name, description, valueOnly);
	}
	
	public static StdoutPortDescriptor parseJson(String name, String description, boolean valueOnly,
													ObjectNode topNode) {
		return new StdoutPortDescriptor(name, description, valueOnly);
	}

	@Override
	public String toStringExpr() {
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s", valueOnlyMark, getPortType().getId());
	}
}
