package mdt.task;

import java.io.File;
import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import utils.io.IOUtils;

import mdt.model.AASUtils;
import mdt.model.resource.value.PropertyValue;
import mdt.model.resource.value.PropertyValues.StringValue;
import mdt.model.workflow.descriptor.port.PortDirection;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class FilePort extends AbstractPort implements Port {
	private static final Logger s_logger = LoggerFactory.getLogger(FilePort.class);
	
	private String m_path;
	
	public FilePort(String name, PortDirection type, boolean valueOnly, String path) {
		super(name, type, valueOnly);
		
		m_path = path;
		setLogger(s_logger);
	}
	
	@Override
	public String getJsonString() {
		return AASUtils.writeJson(getAsJsonObject());
	}
	
	@Override
	public JsonNode getJsonNode() {
		return AASUtils.getJsonSerializer().toNode(getAsJsonObject());
	}
	
	public Object getAsJsonObject() {
		return AASUtils.readJsonNode(new File(m_path));
	}
	
	public SubmodelElement getSubmodelElement() {
		return AASUtils.readJson(new File(m_path), SubmodelElement.class);
	}

	public Object getRawValue() {
		if ( isValuePort() ) {
			File portFile = new File(m_path);
			try {
				String jsonStr = IOUtils.toString(portFile).trim();
				if ( jsonStr.startsWith("{") ) {
					return AASUtils.readJson(jsonStr, PropertyValue.class);
				}
				else {
					return new StringValue(jsonStr);
				}
			}
			catch ( IOException e ) {
				throw new InternalError("Failed to read FilePort: file=" + portFile + ", cause=" + e);
			}
		}
		else {
			return AASUtils.readJson(new File(m_path), SubmodelElement.class);
		}
	}

	@Override
	public void setJsonNode(JsonNode node) {
		setJsonString(AASUtils.toJsonString(node));
	}

	@Override
	public void setJsonString(String jsonString) {
		try {
			File file = new File(m_path);
			
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("write json to parameter port: file={}, json={}", file, jsonString);
			}
			IOUtils.toFile(jsonString, file);
		}
		catch ( IOException e ) {
			throw new AssertionError("" + e);
		}
	}
	
	@Override
	public String toString() {
		return String.format("[%s(%s)] file=%s", m_name, m_direction.getTypeString(), new File(m_path));
	}
}