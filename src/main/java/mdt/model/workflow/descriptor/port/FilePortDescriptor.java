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
@JsonPropertyOrder({"name", "description", "portType", "path", "valueOnly"})
public class FilePortDescriptor extends AbstractPortDescriptor {
	@JsonProperty("path") private String m_path;

	public FilePortDescriptor() { }
	public FilePortDescriptor(String name, String description, String path, boolean valueOnly) {
		super(name, description, PortType.FILE, valueOnly);
		
		m_path = path;
	}
	
	public String getPath() {
		return m_path;
	}
	
	public void setPath(String path) {
		m_path = path;
	}

	public static FilePortDescriptor parseStringExpr(String name, String description, String valueExpr,
														boolean valueOnly) {
		return new FilePortDescriptor(name, description, valueExpr, valueOnly);
	}
	
	public static FilePortDescriptor parseJson(String name, String description, boolean valueOnly,
															ObjectNode topNode) {
		String path = topNode.get("path").asText();

		return new FilePortDescriptor(name, description, path, valueOnly);
	}

	@Override
	public String toStringExpr() {
		String valueOnlyMark = isValueOnly() ? "*" : "";
		return String.format("%s%s:%s", valueOnlyMark, getPortType().getId(), m_path);
	}
}
