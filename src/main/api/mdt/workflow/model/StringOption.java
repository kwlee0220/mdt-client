package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class StringOption extends AbstractOption<String> {
	public static final String SERIALIZATION_TYPE = "mdt:option:string";
	
	public StringOption(String name, String value) {
		super(name, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()), getValue());
	}
	
	public static StringOption deserializeFields(JsonNode jnode) {
		String name = JacksonUtils.getStringField(jnode, "name");
		String value = JacksonUtils.getStringField(jnode, "value");
		return new StringOption(name, value);
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeStringField("value", getValue());
	}
}
