package mdt.model.sm.value;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.json.JacksonUtils;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementCollectionValue extends AbstractElementValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:collection";
	
	private final LinkedHashMap<String,? extends ElementValue> m_fields;
	
	public ElementCollectionValue(Map<String,? extends ElementValue> elements) {
		m_fields = Maps.newLinkedHashMap(elements);
	}
	
	public Map<String,? extends ElementValue> getFieldAll() {
		return m_fields;
	}
	
	public boolean containsField(String fieldName) {
		return m_fields.containsKey(fieldName);
	}

	public Optional<ElementValue> findField(String fieldName) {
		return Optional.ofNullable(m_fields.get(fieldName));
	}

	public ElementValue getField(String fieldName) {
		ElementValue field = m_fields.get(fieldName);
		if ( field != null ) {
			return field;
		}
		else {
			throw new IllegalArgumentException("No field found: " + fieldName);
		}
	}
	
	public static ElementCollectionValue parseValueJsonNode(SubmodelElementCollection smc, JsonNode vnode)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("JsonNode is not 'Object' node, JsonNode=" + vnode);
		}
		
		Map<String,ElementValue> values = Maps.newLinkedHashMap();
		for ( SubmodelElement elmNode : smc.getValue() ) {
			JsonNode field = JacksonUtils.getFieldOrNull(vnode, elmNode.getIdShort());
			if ( field != null ) {
				values.put(elmNode.getIdShort(), ElementValues.parseValueJsonNode(elmNode, field));
			}
		}
		return new ElementCollectionValue(values);
	}

	@Override
	public Object toValueJsonObject() {
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
		var fields = FStream.from(jnode.properties())
							.mapToKeyValue(ent -> KeyValue.of(ent.getKey(), ent.getValue()))
							.mapValue(ElementValues::parseJsonNode)
							.toMap();
		return new ElementCollectionValue(fields);
	}
}
