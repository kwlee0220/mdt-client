package mdt.workflow.model.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.func.Optionals;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType", "holder", "variable"})
public class WorkflowVariablePortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("holder") private String m_holder;
	@JsonProperty("variable") private String m_variable;

	public WorkflowVariablePortDescriptor() { }
	public WorkflowVariablePortDescriptor(String name, String description, String holder, String variable) {
		super(name, description, PortType.VARIABLE);
		
		m_holder = holder;
		m_variable = variable;
	}
	
	public String getHolder() {
		return m_holder;
	}
	
	public void setHolder(String holder) {
		m_holder = holder;
	}
	
	public String getVariable() {
		return m_variable;
	}
	
	public void setVariable(String variable) {
		m_variable = variable;
	}

	public static WorkflowVariablePortDescriptor parseStringExpr(String name, String description, String valueExpr) {
		String[] parts = valueExpr.split("/");
		if ( parts.length != 2 ) {
			throw new IllegalArgumentException("Invalid WorkflowVariablePortDescriptor string: " + valueExpr);
		}
		
		return new WorkflowVariablePortDescriptor(name, description, parts[0], parts[1]);
	}
	
	public static WorkflowVariablePortDescriptor parseJson(String name, String description, ObjectNode topNode) {
		JsonNode holderNode = topNode.get("holder");
		String holder = (holderNode != null) ? holderNode.asText() : null;
		String variable = topNode.get("variable").asText();

		return new WorkflowVariablePortDescriptor(name, description, holder, variable);
	}
	
	@Override
	public String toStringExpr() {
		String holderStr = Optionals.mapOrElse(m_holder, s -> String.format("%s/", s), "");
		return String.format("%s:%s%s", getPortType().getId(), holderStr, m_variable);
	}
}
