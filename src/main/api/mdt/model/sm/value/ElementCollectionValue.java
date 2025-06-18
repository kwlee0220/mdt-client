package mdt.model.sm.value;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.InternalException;
import utils.KeyValue;
import utils.func.FOption;
import utils.func.Unchecked;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementCollectionValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:collection";
	
	private final Map<String,ElementValue> m_fields;
	
	public ElementCollectionValue(Map<String,ElementValue> elements) {
		m_fields = elements;
	}
	
	public Map<String,ElementValue> getFieldAll() {
		return m_fields;
	}
	
	public boolean containsField(String fieldName) {
		return m_fields.containsKey(fieldName);
	}

	public FOption<ElementValue> findField(String fieldName) {
		return KeyValueFStream.from(m_fields)
						.findFirst(kv -> kv.key().equals(fieldName))
						.map(KeyValue::value);
	}

	public ElementValue getField(String fieldName) {
		return KeyValueFStream.from(m_fields)
						.findFirst(kv -> kv.key().equals(fieldName))
						.map(KeyValue::value)
						.getOrThrow(() -> new IllegalArgumentException("No field found: " + fieldName));
	}
	
	public static ElementCollectionValue deserializeFields(JsonNode jnode, ElementCollectionValue proto) throws IOException {
		var fields = FStream.from(jnode.fields())
							.mapToKeyValue(ent -> KeyValue.of(ent.getKey(), ent.getValue()))
					        .innerJoin(KeyValueFStream.from(proto.m_fields))
							.mapValue(match -> {
								try {
									JsonNode n = match._1;
									Class<? extends ElementValue> cls = match._2.getClass();
									return (ElementValue)MethodUtils.invokeStaticMethod(cls, "parseJsonNode", n);
								}
								catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
									throw new InternalException("Failed to parse JSON node", e);
								}
							})
							.toMap();
		return new ElementCollectionValue(fields);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		KeyValueFStream.from(m_fields)
				.forEach((k, v) -> Unchecked.runOrThrowSneakily(() -> gen.writeObjectField(k, v)));
		gen.writeEndObject();
	}
}
