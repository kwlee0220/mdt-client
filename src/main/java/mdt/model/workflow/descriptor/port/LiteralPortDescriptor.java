package mdt.model.workflow.descriptor.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType", "literal", "valueOnly"})
public class LiteralPortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("literal") private String m_literal;

	public LiteralPortDescriptor() { }
	public LiteralPortDescriptor(String name, String description, String literal, boolean valueOnly) {
		super(name, description, PortType.LITERAL, valueOnly);
		
		m_literal = literal;
	}
	
	public String getLiteral() {
		return m_literal;
	}
	
	public void setLiteral(String literal) {
		m_literal = literal;
	}

	public static LiteralPortDescriptor parseStringExpr(String name, String description, String valueExpr,
														boolean valueOnly) {
		return new LiteralPortDescriptor(name, description, valueExpr, valueOnly);
	}
	
	public static LiteralPortDescriptor parseJson(String name, String description, boolean valueOnly,
													ObjectNode topNode) {
		String literal = topNode.get("literal").asText();

		return new LiteralPortDescriptor(name, description, literal, valueOnly);
	}

	@Override
	public String toStringExpr() {
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s:%s", valueOnlyMark, getPortType().getId(), m_literal);
	}
}
