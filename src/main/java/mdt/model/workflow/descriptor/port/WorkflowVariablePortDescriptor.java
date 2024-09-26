package mdt.model.workflow.descriptor.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.func.FOption;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "description", "portType", "holder", "variable", "valueOnly"})
public class WorkflowVariablePortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("holder") private String m_holder;
	@JsonProperty("variable") private String m_variable;

	public WorkflowVariablePortDescriptor() { }
	public WorkflowVariablePortDescriptor(String name, String description, String holder, String variable,
											boolean valueOnly) {
		super(name, description, PortType.VARIABLE, valueOnly);
		
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

	public static WorkflowVariablePortDescriptor parseStringExpr(String name, String description, String valueExpr,
																boolean valueOnly) {
		String[] parts = valueExpr.split("/");
		if ( parts.length != 2 ) {
			throw new IllegalArgumentException("Invalid WorkflowVariablePortDescriptor string: " + valueExpr);
		}
		
		return new WorkflowVariablePortDescriptor(name, description, parts[0], parts[1], valueOnly);
	}
	
	public static WorkflowVariablePortDescriptor parseJson(String name, String description, boolean valueOnly,
															ObjectNode topNode) {
		String holder = FOption.map(topNode.get("holder"), JsonNode::asText); 
		String variable = topNode.get("variable").asText();

		return new WorkflowVariablePortDescriptor(name, description, holder, variable, valueOnly);
	}
	
	@Override
	public String toStringExpr() {
		String holderStr = FOption.mapOrElse(m_holder, s -> String.format("%s/", s), "");
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s:%s%s", valueOnlyMark, getPortType().getId(), holderStr, m_variable);
	}
}
