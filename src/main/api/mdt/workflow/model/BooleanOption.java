package mdt.workflow.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BooleanOption extends AbstractOption<Boolean> {
	public static final String SERIALIZATION_TYPE = "mdt:option:boolean";
	
	public BooleanOption(String name, Boolean value) {
		super(name, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		if ( getValue() ) {
			return List.of(String.format("--%s", getName()));
		}
		else {
			return Collections.emptyList();
		}
	}
	
	public static BooleanOption deserializeFields(JsonNode jnode) {
		String name = JacksonUtils.getStringField(jnode, "name");
		Boolean value = JacksonUtils.getBooleanFieldOrNull(jnode, "value");
		return new BooleanOption(name, value);
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeBooleanField("value", getValue());
	}
}
