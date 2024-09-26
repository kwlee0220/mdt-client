package mdt.model.workflow.descriptor.port;

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
@JsonPropertyOrder({"name", "description", "portType", "mdtId", "parameterId", "valueOnly"})
public class MDTParameterPortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("mdtId") private String m_mdtId;
	@JsonProperty("parameterId") private String m_parameterId;

	public MDTParameterPortDescriptor() { }
	public MDTParameterPortDescriptor(String name, String description, String mdtId, String parameterId,
										boolean valueOnly) {
		super(name, description, PortType.PARAMETER, valueOnly);
		
		m_mdtId = mdtId;
		m_parameterId = parameterId;
	}
	
	public String getMdtId() {
		return m_mdtId;
	}
	
	public void setMdtId(String id) {
		m_mdtId = id;
	}
	
	public String getParameterId() {
		return m_parameterId;
	}
	
	public void setParameterId(String idShort) {
		m_parameterId = idShort;
	}

	public static MDTParameterPortDescriptor parseStringExpr(String name, String description, String valueExpr,
																boolean valueOnly) {
		String[] parts = valueExpr.split("/");
		if ( parts.length != 2 ) {
			throw new IllegalArgumentException("Invalid MDTParameterPortDescriptor string: " + valueExpr);
		}
		
		return new MDTParameterPortDescriptor(name, description, parts[0], parts[1], valueOnly);
	}
	
	public static MDTParameterPortDescriptor parseJson(String name, String description, boolean valueOnly,
															ObjectNode topNode) {
		String mdtId = topNode.get("mdtId").asText();
		String parameterId = topNode.get("parameterId").asText();

		return new MDTParameterPortDescriptor(name, description, mdtId, parameterId, valueOnly);
	}
	
	@Override
	public String toStringExpr() {
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s:%s/%s", valueOnlyMark, getPortType().getId(), m_mdtId, m_parameterId);
	}
}
