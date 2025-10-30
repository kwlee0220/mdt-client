package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonDeserialize(using = ElementValues.Deserializer.class)
@JsonSerialize(using = ElementValues.Serializer.class)
public abstract class AbstractElementValue implements ElementValue {
	abstract public String getSerializationType();
	abstract public void serializeValue(JsonGenerator gen) throws IOException;
	// public static ElementValue deserializeValue(JsonNode vnode) throws IOException;	// ElementValues 클래스에서 정의됨.

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}

	@Override
	public String toValueJsonString() {
		try {
			return MDTModelSerDe.getJsonMapper().writeValueAsString(toValueJsonObject());
		}
		catch ( IOException e ) {
			throw new RuntimeException("Failed to get valueString of ElementValue: cause=" + e, e);
		}
	}

	@Override
	public String toDisplayString() {
		// PropertyValue인 경우는 재정의됨.
		try {
			return MDTModelSerDe.getJsonSerializer().write(toValueJsonObject());
		}
		catch ( SerializationException e ) {
			throw new RuntimeException("Failed to get displayString of ElementValue: cause=" + e, e);
		}
	}
	
	@Override
	public String toString() {
		return toDisplayString();
	}
}
