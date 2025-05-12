package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MultiLineOption extends AbstractOption<List<String>> {
	public static final String SERIALIZATION_TYPE = "mdt:option:multi-line";
	
	public MultiLineOption(String name, List<String> value) {
		super(name, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		String mlines = FStream.from(getValue()).join(System.lineSeparator());
		return List.of("--" + getName(), mlines);
	}
	
	public static MultiLineOption deserializeFields(JsonNode jnode) {
		String name = JacksonUtils.getStringField(jnode, "name");
		JsonNode linesJnode = jnode.get("value");
		if ( linesJnode.isArray() ) {
			List<String> lines = FStream.from(linesJnode).map(JsonNode::asText).toList();
			return new MultiLineOption(name, lines);
		}
		else {
			return new MultiLineOption(name, List.of(jnode.asText()));
		}
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeArrayFieldStart("value");
		FStream.from(getValue()).forEachOrThrow(gen::writeString);
		gen.writeEndArray();
	}
}
