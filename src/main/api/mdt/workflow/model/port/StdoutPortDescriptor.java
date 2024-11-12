package mdt.workflow.model.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType"})
public class StdoutPortDescriptor extends AbstractPortDescriptor {
	public StdoutPortDescriptor() { }
	public StdoutPortDescriptor(String name, String description) {
		super(name, description, PortType.STDOUT);
	}

	public static StdoutPortDescriptor parseStringExpr(String name, String description) {
		return new StdoutPortDescriptor(name, description);
	}
	
	public static StdoutPortDescriptor parseJson(String name, String description, ObjectNode topNode) {
		return new StdoutPortDescriptor(name, description);
	}

	@Override
	public String toStringExpr() {
		return String.format("%s", getPortType().getId());
	}
}
