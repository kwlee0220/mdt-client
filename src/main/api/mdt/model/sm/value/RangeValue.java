package mdt.model.sm.value;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@AllArgsConstructor
public final class RangeValue implements DataElementValue {
	private String min;
	private String max;
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("min", this.min);
		gen.writeStringField("max", this.max);
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("%s:%s", this.min, this.max);
	}
}
