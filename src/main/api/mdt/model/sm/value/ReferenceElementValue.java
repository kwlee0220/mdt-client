package mdt.model.sm.value;

import java.io.IOException;
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

import utils.Preconditions;
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
		Preconditions.checkNotNullArgument(reference, "reference is null");

		m_reference = reference;
	}
	
	public static ReferenceElementValue from(ReferenceElement refElm) {
		return new ReferenceElementValue(refElm.getValue());
	}
	
	public Reference getReference() {
		return m_reference;
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<Map<String,String>> toValueObject() {
		return FStream.from(m_reference.getKeys())
						.map(key -> {
							Map<String,String> kvMap = Maps.newLinkedHashMap();
							kvMap.put("type", key.getType().name());
							kvMap.put("value", key.getValue());
							return kvMap;
						})
						.toList();
	}
	
	public void update(ReferenceElement refElm) {
		refElm.setValue(m_reference);
	}
	
	public static ReferenceElementValue fromValueObject(Object value, ReferenceTypes refTypes) throws IOException {
		if ( value instanceof List keyObjsList ) {
			DefaultReference.Builder builder = new DefaultReference.Builder().type(refTypes);
			@SuppressWarnings("unchecked")
			List<Key> keys = FStream.<Map<?, ?>>from(keyObjsList)
									.map(keyObj -> {
										Map<?, ?> keyMap = (Map<?, ?>) keyObj;
										String keyType = (String) keyMap.get("type");
										String keyValue = (String) keyMap.get("value");
										return (Key) new DefaultKey.Builder()
																	.type(KeyTypes.valueOf(keyType))
																	.value(keyValue)
																	.build();
									})
									.toList();
			DefaultReference ref = builder.keys(keys).build();
			return new ReferenceElementValue(ref);
		}
		else {
			throw new IOException("invalid ReferenceElementValue object: " + value);
		}
	}
	
	public static ReferenceElementValue parseValueJsonNode(JsonNode vnode, ReferenceTypes refTypes)
		throws IOException {
		JsonNode keysNode;
		if ( vnode.isArray() ) {
			keysNode = vnode;
		}
		else if ( vnode.isObject() && vnode.has("keys") ) {
			keysNode = vnode.get("keys");
		}
		else {
			throw new IOException("ReferenceElementValue expects a key 'Array' node: JsonNode=" + vnode);
		}

		Reference ref = new DefaultReference.Builder()
								.type(refTypes)
								.keys(parseKeysNode(keysNode))
								.build();
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
	
	public static ReferenceElementValue deserializeValue(JsonNode vnode) throws IOException {
		String type = JacksonUtils.getStringField(vnode, "type");
		JsonNode keysNode = JacksonUtils.getFieldOrNull(vnode, "keys");
		if ( keysNode == null || !keysNode.isArray() ) {
			throw new IOException("ReferenceElementValue expects a 'keys' Array field: JsonNode=" + vnode);
		}

		Reference ref = new DefaultReference.Builder()
								.type(ReferenceTypes.valueOf(type))
								.keys(parseKeysNode(keysNode))
								.build();
		return new ReferenceElementValue(ref);
	}

	private static List<Key> parseKeysNode(JsonNode keysNode) {
		return FStream.from(keysNode.elements())
						.mapOrThrow(keyNode -> {
							String keyType = JacksonUtils.getStringField(keyNode, "type");
							String keyValue = JacksonUtils.getStringField(keyNode, "value");
							return (Key)new DefaultKey.Builder()
														.type(KeyTypes.valueOf(keyType))
														.value(keyValue)
														.build();
						})
						.toList();
	}
}
