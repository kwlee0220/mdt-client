package mdt.workflow.model;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.UnitUtils;
import utils.json.JacksonUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DurationOption extends AbstractOption<Duration> {
	public static final String SERIALIZATION_TYPE = "mdt:option:duration";
	
	public DurationOption(String name, Duration value) {
		super(name, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of("--" + getName(), getValue().toString());
	}
	
	public static DurationOption deserializeFields(JsonNode jnode) {
		String name = JacksonUtils.getStringField(jnode, "name");
		String value = JacksonUtils.getStringField(jnode, "value");
		return new DurationOption(name, UnitUtils.parseDuration(value));
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeStringField("value", getValue().toString());
	}
}
