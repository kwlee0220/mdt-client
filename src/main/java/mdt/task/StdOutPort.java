package mdt.task;

import com.fasterxml.jackson.databind.JsonNode;

import mdt.model.AASUtils;
import mdt.model.workflow.descriptor.port.PortDirection;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class StdOutPort extends AbstractPort implements Port {
	public StdOutPort(String name, boolean valueOnly) {
		super(name, PortDirection.OUTPUT, valueOnly);
	}

	@Override
	public JsonNode getJsonNode() {
		throw new UnsupportedOperationException("StdOutPort.getJsonNode()");
	}

	@Override
	public String getJsonString() {
		throw new UnsupportedOperationException("StdOutPort.getJsonString()");
	}

	@Override
	public void setJsonNode(JsonNode node) {
		System.out.println(AASUtils.writeJson(node));
	}

	@Override
	public void setJsonString(String jsonStr) {
		System.out.println(jsonStr);
	}
}
