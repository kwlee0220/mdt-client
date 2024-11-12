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
@JsonPropertyOrder({"name", "description"})
public class PortDeclaration extends AbstractPortDescriptor {
	public PortDeclaration() { }
	public PortDeclaration(String name, String description) {
		super(name, description, PortType.DECLARE);
	}

	public static PortDeclaration parseStringExpr(String name, String description) {
		return new PortDeclaration(name, description);
	}
	
	public static PortDeclaration parseJson(String name, String description, ObjectNode topNode) {
		return new PortDeclaration(name, description);
	}

	@Override
	public String toStringExpr() {
		return String.format("%s", getPortType().getId());
	}
}
