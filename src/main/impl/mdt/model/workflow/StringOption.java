package mdt.model.workflow;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

import mdt.workflow.model.OptionValueType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class StringOption extends AbstractOption {
	private final String value;
	
	public StringOption(String name, String value) {
		super(name);
		this.value = value;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()), this.value);
	}
	
	public static StringOption parseJson(String name, JsonNode jnode) {
		return new StringOption(name, jnode.get("value").asText());
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.string.name());
		
		gen.writeStringField("value", this.value);
		
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("Option[%s]: %s%n", getName(), getValue());
	}
}
