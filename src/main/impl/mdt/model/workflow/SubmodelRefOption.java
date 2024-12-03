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
public final class SubmodelRefOption extends AbstractOption {
	private final String twinId;
	private final String submodelIdShort;
	
	@JsonCreator
	public SubmodelRefOption(@JsonProperty("name") String name,
									@JsonProperty("twinId") String twinId,
									@JsonProperty("submodelIdShort") String submodelIdShort) {
		super(name);
		Preconditions.checkNotNull(twinId != null);
		Preconditions.checkNotNull(submodelIdShort != null);
		
		this.twinId = twinId;
		this.submodelIdShort = submodelIdShort;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()),
						String.format("%s/%s", this.twinId, this.submodelIdShort));
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("name", getName());
		gen.writeStringField("optionType", OptionValueType.submodel_ref.name());

		gen.writeStringField("twinId", this.twinId);
		gen.writeStringField("submodelIdShort", this.submodelIdShort);
		
		gen.writeEndObject();
	}
	
	public static SubmodelRefOption parseJson(String name, JsonNode jnode) {
		return new SubmodelRefOption(name, jnode.get("twinId").asText(),
											jnode.get("submodelIdShort").asText());
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", this.twinId, this.submodelIdShort);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.twinId, this.submodelIdShort);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		SubmodelRefOption other = (SubmodelRefOption)obj;
		return Objects.equals(this.twinId, other.twinId)
				&& Objects.equals(this.submodelIdShort, other.submodelIdShort);
	}
}
