package mdt.model.workflow;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;

import lombok.Getter;

import mdt.workflow.model.OptionValueType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class MultilineOption extends AbstractOption {
	private final String lines;
	
	public static MultilineOption parseJson(String name, JsonNode jnode) {
		return new MultilineOption(name, jnode.get("lines").asText());
	}
	
	@JsonCreator
	public MultilineOption(@JsonProperty("name") String name, @JsonProperty("lines") String lines) {
		super(name);
		
		this.lines = lines;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()), this.lines);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.multiline.name());
		
		gen.writeStringField("lines", this.lines);
		
		gen.writeEndObject();
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		MultilineOption other = (MultilineOption)obj;
		return Objects.equal(this.lines, other.lines);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.lines);
	}
	
	@Override
	public String toString() {
		return this.lines;
	}
}
