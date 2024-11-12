package mdt.model.sm.value;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@SuppressWarnings("serial")
public class SubmodelElementValueSerializer extends StdSerializer<SubmodelElementValue> {
	private SubmodelElementValueSerializer() {
		this(null);
	}
	private SubmodelElementValueSerializer(Class<SubmodelElementValue> cls) {
		super(cls);
	}
	
	@Override
	public void serialize(SubmodelElementValue smev, JsonGenerator gen, SerializerProvider provider)
		throws IOException, JsonProcessingException {
		smev.serialize(gen);
	}
}
