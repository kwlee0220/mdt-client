package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ReferenceElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:reference";
	
	private final Reference m_reference;
	
	public ReferenceElementValue(Reference reference) {
		m_reference = reference;
	}
	
	public Reference getReference() {
		return m_reference;
	}
	
	public static ReferenceElementValue parseJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringField(jnode, "type");
		if ( type == null ) {
			throw new IOException("missing field: Reference type");
		}
		
		DefaultReference.Builder builder = new DefaultReference.Builder()
												.type(ReferenceTypes.valueOf(type));
		FStream.from(jnode.get("keys").elements())
	            .map(keyNode -> {
	                String keyType = JacksonUtils.getStringField(keyNode, "type");
	                String keyValue = JacksonUtils.getStringField(keyNode, "value");
	                return (Key)new DefaultKey.Builder()
				                            .type(KeyTypes.valueOf(keyType))
				                            .value(keyValue)
				                            .build();
	            })
	            .forEach(key -> builder.keys(key));
		Reference ref = builder.build();
		return new ReferenceElementValue(ref);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
			gen.writeStringField("type", m_reference.getType().name());
			gen.writeArrayFieldStart("keys");
				for ( Key key: m_reference.getKeys() ) {
					gen.writeStartObject();
					gen.writeStringField("type", key.getType().name());
					gen.writeStringField("value", key.getValue());
					gen.writeEndObject();
				}
			gen.writeEndArray();
		gen.writeEndObject();
	}
}
