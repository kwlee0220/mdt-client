package mdt.workflow.model;

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
public class OptionSerializer extends StdSerializer<Option> {
	private OptionSerializer() {
		this(null);
	}
	private OptionSerializer(Class<Option> cls) {
		super(cls);
	}
	
	@Override
	public void serialize(Option option, JsonGenerator gen, SerializerProvider provider)
		throws IOException, JsonProcessingException {
		option.serialize(gen);
	}
}
