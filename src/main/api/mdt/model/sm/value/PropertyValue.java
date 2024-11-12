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
public class PropertyValue implements DataElementValue {
	private String value;
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeString(value);
	}
}
