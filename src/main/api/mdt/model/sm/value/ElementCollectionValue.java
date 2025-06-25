package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.KeyValue;
import utils.func.FOption;
import utils.json.JacksonUtils;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementCollectionValue extends AbstractElementValue implements ElementValue {
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
	
	public static ElementCollectionValue parseValueJsonNode(SubmodelElementCollection smc, JsonNode vnode) {
		Map<String,ElementValue> values
			= FStream.from(smc.getValue())
					.flatMapNullable(elmNode -> {
						JsonNode field = JacksonUtils.getFieldOrNull(vnode, elmNode.getIdShort());
						if ( field != null ) {
							return KeyValue.of(elmNode.getIdShort(), ElementValues.parseValueJsonNode(elmNode, field));
						}
						else {
							return null;
						}
					})
					.toKeyValueStream(kv -> kv)
					.toMap();
		return new ElementCollectionValue(values);
	}

	@Override
	protected Object toValueJsonObject() {
		return KeyValueFStream.from(m_fields)
								.mapValue(elmVal -> ((AbstractElementValue)elmVal).toValueJsonObject())
								.toMap();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_fields);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || ElementCollectionValue.class != obj.getClass() ) {
			return false;
		}
		
		ElementCollectionValue other = (ElementCollectionValue) obj;
		return Objects.equals(m_fields, other.m_fields);
	}
	
	@Override
	public String toString() {
		return m_fields.toString();
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		KeyValueFStream.from(m_fields)
						.forEachOrThrow(kv -> gen.writeObjectField(kv.key(), kv.value()));
		gen.writeEndObject();
	}
	
	public static ElementCollectionValue deserializeFields(JsonNode jnode) throws IOException {
		var fields = FStream.from(jnode.fields())
							.mapToKeyValue(ent -> KeyValue.of(ent.getKey(), ent.getValue()))
							.mapValue(ElementValues::parseJsonNode)
							.toMap();
		return new ElementCollectionValue(fields);
	}
}
