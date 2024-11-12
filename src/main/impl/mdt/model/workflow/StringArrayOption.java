package mdt.model.workflow;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import mdt.workflow.model.OptionValueType;

import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class StringArrayOption extends AbstractOption {
	private final List<String> values;
	
	@JsonCreator
	public StringArrayOption(@JsonProperty("name") String name, @JsonProperty("value") List<String> values) {
		super(name);
		
		this.values = values;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return FStream.of(String.format("--%s", getName()))
						.concatWith(FStream.from(this.values))
						.toList();
	}
	
	public static StringArrayOption parseJson(String name, JsonNode jnode) {
		List<String> strList = FStream.from(jnode.get("values").elements())
										.map(JsonNode::asText)
										.toList();
		return new StringArrayOption(name, strList);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.array.name());
		
		gen.writeArrayFieldStart("values");
		for ( String v: this.values ) {
			gen.writeString(v);
		}
		gen.writeEndArray();
		
		gen.writeEndObject();
	}
}
