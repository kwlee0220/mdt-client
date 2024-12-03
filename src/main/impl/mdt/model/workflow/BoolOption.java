package mdt.model.workflow;

import java.io.IOException;
import java.util.Collections;
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
public class BoolOption extends AbstractOption {
	private final boolean value;
	
	@JsonCreator
	public BoolOption(@JsonProperty("name") String name, @JsonProperty("value") boolean value) {
		super(name);
		
		this.value = value;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		if ( this.value ) {
			return List.of(String.format("--%s", getName()));
		}
		else {
			return Collections.emptyList();
		}
	}
	
	public static BoolOption parseJson(String name, JsonNode jnode) {
		return new BoolOption(name, Boolean.parseBoolean(jnode.get("value").asText()));
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.bool.name());
		
		gen.writeBooleanField("value", this.value);
		
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
		
		BoolOption other = (BoolOption)obj;
		return Objects.equal(this.value, other.value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.value);
	}
	
	@Override
	public String toString() {
		return "" + this.value;
	}
}
