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
@JsonPropertyOrder({"name", "description", "portType", "path"})
public class FilePortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("path") private String m_path;

	public FilePortDescriptor() { }
	public FilePortDescriptor(String name, String description, String path) {
		super(name, description, PortType.FILE);
		
		m_path = path;
	}
	
	public String getPath() {
		return m_path;
	}
	
	public void setPath(String path) {
		m_path = path;
	}

	public static FilePortDescriptor parseStringExpr(String name, String description, String valueExpr) {
		return new FilePortDescriptor(name, description, valueExpr);
	}
	
	public static FilePortDescriptor parseJson(String name, String description, ObjectNode topNode) {
		String path = topNode.get("path").asText();

		return new FilePortDescriptor(name, description, path);
	}

	@Override
	public String toStringExpr() {
		return String.format("%s:%s", getPortType().getId(), m_path);
	}
}
