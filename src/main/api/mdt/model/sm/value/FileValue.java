package mdt.model.sm.value;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import lombok.Data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
public final class FileValue implements DataElementValue {
	private String contentType;
	private String value;
	
	public FileValue(String contentType, String value) {
		this.contentType = contentType;
		this.value = value;
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("contentType", this.contentType);
		gen.writeStringField("value", this.value);
		gen.writeEndObject();
	}
}
