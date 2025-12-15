package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.tika.Tika;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.digitaltwin.aas4j.v3.model.File;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class FileValue extends AbstractElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:file";
	
	private final String m_contentType;
	private final String m_value;
	
	public FileValue(String path, @NonNull String contentType) {
		m_contentType = contentType;
		m_value = path;
	}
	
	public static FileValue of(java.io.File file) throws IOException {
		return new FileValue(file.getName(), new Tika().detect(file));
	}
	
	public String getMimeType() {
		return m_contentType;
	}
	
	public String getValue() {
		return m_value;
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}
	
	public static FileValue parseValueJsonNode(File aasFile, JsonNode jnode) {
		String contentType = JacksonUtils.getStringField(jnode, FIELD_CONTENT_TYPE);
		String value = JacksonUtils.getStringFieldOrNull(jnode, FIELD_VALUE);
		
		return new FileValue(value, contentType);
	}

	@Override
	public Object toValueJsonObject() {
		Map<String,String> value = Maps.newLinkedHashMap();
		value.put(FIELD_CONTENT_TYPE, m_contentType);
		value.put(FIELD_VALUE, m_value);
		return value;
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
		return String.format("file:'%s' ('%s')", (m_value != null) ? m_value : "None", this.m_contentType);
	}

	private static final String FIELD_CONTENT_TYPE = "contentType";
	private static final String FIELD_VALUE = "value";

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_CONTENT_TYPE, m_contentType);
		gen.writeStringField(FIELD_VALUE, m_value);
		gen.writeEndObject();
	}
	
	public static FileValue deserializeValue(JsonNode vnode) {
		String mimeType = JacksonUtils.getStringField(vnode, FIELD_CONTENT_TYPE);
		String value = JacksonUtils.getStringField(vnode, FIELD_VALUE);
		return new FileValue(value, mimeType);
	}
	
	public static final void main(String... args) throws Exception {
		FileValue val = new FileValue(null, "text/plain");
		String json = val.toJsonString();
		json = val.toValueJsonString();
		
		System.out.println("json=" + json);
	}
}
