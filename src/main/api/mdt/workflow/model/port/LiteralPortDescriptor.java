package mdt.workflow.model.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType", "literal"})
public class LiteralPortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("literal") private String m_literal;

	public LiteralPortDescriptor() { }
	public LiteralPortDescriptor(String name, String description, String literal) {
		super(name, description, PortType.LITERAL);
		
		m_literal = literal;
	}
	
	public String getLiteral() {
		return m_literal;
	}
	
	public void setLiteral(String literal) {
		m_literal = literal;
	}

	public static LiteralPortDescriptor parseStringExpr(String name, String description, String valueExpr) {
		return new LiteralPortDescriptor(name, description, valueExpr);
	}
	
	public static LiteralPortDescriptor parseJson(String name, String description, ObjectNode topNode) {
		String literal = topNode.get("literal").asText();

		return new LiteralPortDescriptor(name, description, literal);
	}

	@Override
	public String toStringExpr() {
		return String.format("%s:%s", getPortType().getId(), m_literal);
	}
}
