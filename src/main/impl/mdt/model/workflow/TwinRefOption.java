package mdt.model.workflow;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import lombok.Getter;
import mdt.workflow.model.OptionValueType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public final class TwinRefOption extends AbstractOption {
	private final String twinId;
	
	@JsonCreator
	public TwinRefOption(@JsonProperty("name") String name, @JsonProperty("twinId") String twinId) {
		super(name);
		Preconditions.checkNotNull(twinId != null);
		
		this.twinId = twinId;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()),
						String.format("%s", this.twinId));
	}
	
	public static TwinRefOption parseJson(String name, JsonNode jnode) {
		return new TwinRefOption(name, jnode.get("twinId").asText());
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.twin_ref.name());

		gen.writeStringField("twinId", this.twinId);
		
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("%s", this.twinId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.twinId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		TwinRefOption other = (TwinRefOption)obj;
		return Objects.equals(this.twinId, other.twinId);
	}
}
