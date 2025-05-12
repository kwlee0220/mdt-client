package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class FileOption extends AbstractOption<File> {
	public static final String SERIALIZATION_TYPE = "mdt:option:file";
	
	public FileOption(String name, File value) {
		super(name, value);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of("--" + getName(), getValue().getAbsolutePath());
	}
	
	public static FileOption deserializeFields(JsonNode jnode) {
		String name = JacksonUtils.getStringField(jnode, "name");
		String value = JacksonUtils.getStringField(jnode, "value");
		return new FileOption(name, new File(value));
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeStringField("value", getValue().getAbsolutePath());
	}
}
