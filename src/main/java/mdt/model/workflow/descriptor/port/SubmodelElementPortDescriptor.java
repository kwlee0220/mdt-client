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
@JsonPropertyOrder({"name", "description", "portType", "mdtId", "submodelIdShort",
					"smeIdShortPath", "valueOnly"})
public class SubmodelElementPortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("mdtId") private String m_mdtId;
	@JsonProperty("submodelIdShort") private String m_submodelIdShort;
	@JsonProperty("smeIdShortPath") private String m_smeIdShortPath;

	public SubmodelElementPortDescriptor() { }
	public SubmodelElementPortDescriptor(String name, String description, String mdtId, String submodelIdShort,
											String smeIdShortPath, boolean valueOnly) {
		super(name, description, PortType.SME, valueOnly);
		
		m_mdtId = mdtId;
		m_submodelIdShort = submodelIdShort;
		m_smeIdShortPath = smeIdShortPath;
	}
	
	public String getMdtId() {
		return m_mdtId;
	}
	
	public void setMdtId(String id) {
		m_mdtId = id;
	}
	
	public String getSubmodelIdShort() {
		return m_submodelIdShort;
	}
	
	public void setSubmodelIdShort(String idShort) {
		m_submodelIdShort = idShort;
	}
	
	public String getSmeIdShortPath() {
		return m_smeIdShortPath;
	}
	
	public void setSmeIdShortPath(String path) {
		m_smeIdShortPath = path;
	}

	public static SubmodelElementPortDescriptor parseStringExpr(String name, String description, String valueExpr,
																	boolean valueOnly) {
		String[] parts = valueExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("Invalid SubmodelElementPortDescriptor string: " + valueExpr);
		}
		
		return new SubmodelElementPortDescriptor(name, description, parts[0], parts[1], parts[2], valueOnly);
	}
	
	public static SubmodelElementPortDescriptor parseJson(String name, String description, boolean valueOnly,
															ObjectNode topNode) {
		String mdtId = topNode.get("mdtId").asText();
		String submodelIdShort = topNode.get("submodelIdShort").asText();
		String idShortPath = topNode.get("smeIdShortPath").asText();

		return new SubmodelElementPortDescriptor(name, description, mdtId, submodelIdShort, idShortPath, valueOnly);
	}
	
	@Override
	public String toStringExpr() {
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s:%s/%s/%s", valueOnlyMark, getPortType().getId(),
												m_mdtId, m_submodelIdShort, m_smeIdShortPath);
	}
}
