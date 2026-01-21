package mdt.model.sm.value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.json.JacksonUtils;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ReferenceElementValue extends AbstractElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:reference";
	
	private final Reference m_reference;
	
	public ReferenceElementValue(Reference reference) {
		m_reference = reference;
	}
	
	public Reference getReference() {
		return m_reference;
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public Map<String,Object> toValueObject() {
		List<Map<String,String>> keyList = FStream.from(m_reference.getKeys())
													.map(key -> {
														Map<String,String> kvMap = Maps.newLinkedHashMap();
														kvMap.put("type", key.getType().name());
														kvMap.put("value", key.getValue());
														return kvMap;
													})
													.toList();
		Map<String,Object> output = Maps.newLinkedHashMap();
		output.put("type", m_reference.getType().name());
		output.put("keys", keyList);
		return output;
	}
	
	public static ReferenceElementValue fromValueObject(Object value, ReferenceElement refElm) throws IOException {
		if ( value instanceof List keyObjsList ) {
			ReferenceTypes refTypes = refElm.getValue().getType();
			DefaultReference.Builder builder = new DefaultReference.Builder().type(refTypes);
			List<Key> keys = FStream.<Map<?, ?>>from(keyObjsList)
									.map(keyObj -> {
										Map<?, ?> keyMap = (Map<?, ?>) keyObj;
										String keyType = (String) keyMap.get("type");
										String keyValue = (String) keyMap.get("value");
										return (Key) new DefaultKey.Builder().type(KeyTypes.valueOf(keyType)).value(keyValue).build();
									})
									.toList();
			DefaultReference ref = builder.keys(keys).build();
			return new ReferenceElementValue(ref);
		}
		else {
			throw new IOException("invalid ReferenceElementValue object: " + value);
		}
	}
	
	public static ReferenceElementValue parseValueJsonNode(JsonNode vnode, ReferenceElement refElm)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("ReferenceElementValue expects an 'Object' node: JsonNode=" + vnode);
		}
		
		ReferenceTypes refTypes = refElm.getValue().getType();
		DefaultReference.Builder builder = new DefaultReference.Builder()
												.type(refTypes);
		FStream.from(vnode.get("keys").elements())
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
	public void serializeValue(JsonGenerator gen) throws IOException {
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
	
	public static ReferenceElementValue deserializeValue(JsonNode vnode) {
		String type = JacksonUtils.getStringField(vnode, "type");
		if ( type == null ) {
			throw new UncheckedIOException(null, new IOException("missing field: Reference type"));
		}
		
		DefaultReference.Builder builder = new DefaultReference.Builder()
												.type(ReferenceTypes.valueOf(type));
		FStream.from(vnode.get("keys").elements())
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
}
