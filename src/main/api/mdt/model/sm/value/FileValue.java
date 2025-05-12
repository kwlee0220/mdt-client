package mdt.model.sm.value;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.func.FOption;
import utils.json.JacksonUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:file";
	
	private final String m_contentType;
	private final String m_value;
	
	public FileValue(String contentType, String path) {
		m_contentType = contentType;
		m_value = path;
	}
	
	public String getMimeType() {
		return m_contentType;
	}
	
	public String getValue() {
		return m_value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_value, m_contentType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || FileValue.class != obj.getClass() ) {
			return false;
		}
		
		FileValue other = (FileValue) obj;
		return Objects.equals(m_value, other.m_value)
				&& Objects.equals(m_contentType, other.m_contentType);
	}
	
	@Override
	public String toString() {
		return String.format("file:'%s' ('%s')", FOption.getOrElse(this.m_value, "None"), this.m_contentType);
	}

	private static final String FIELD_CONTENT_TYPE = "contentType";
	private static final String FIELD_VALUE = "value";
	
	public static FileValue parseJsonNode(JsonNode jnode) {
		String mimeType = JacksonUtils.getStringField(jnode, FIELD_CONTENT_TYPE);
		String value = JacksonUtils.getStringField(jnode, FIELD_VALUE);
		return new FileValue(mimeType, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_CONTENT_TYPE, m_contentType);
		gen.writeStringField(FIELD_VALUE, m_value);
		gen.writeEndObject();
	}
}
