package mdt.task;

import com.fasterxml.jackson.databind.JsonNode;

import mdt.model.AASUtils;
import mdt.model.workflow.descriptor.port.PortDirection;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class LiteralPort extends AbstractPort {
	private String m_literal;

	public LiteralPort(String name, PortDirection type, boolean valueOnly, String literal) {
		super(name, type, valueOnly);
		
		m_literal = literal;
	}

	@Override
	public boolean isValuePort() {
		return true;
	}

	@Override
	public JsonNode getJsonNode() {
		return AASUtils.readJsonNode(m_literal);
	}

	@Override
	public void setJsonNode(JsonNode node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getJsonString() {
		return null;
	}

	@Override
	public void setJsonString(String jsonStr) {
		throw new UnsupportedOperationException();
	}
}
