package mdt.model.sm.value;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

	@Override
	public Map<String,Object> toValueObject() {
		return KeyValueFStream.from(m_fields)
								.mapValue(ElementValue::toValueObject)
								.toMap();
	}
	
	public Map<String,? extends ElementValue> getFieldMap() {
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
	
	public void update(SubmodelElementCollection smc) {
		FStream.from(smc.getValue())
				.tagKey(v -> v.getIdShort())
				.match(m_fields)
				.forEach(match -> ElementValues.update(match.value()._1, match.value()._2));
	}
	
	public static ElementCollectionValue from(SubmodelElementCollection smc) {
		var members = FStream.from(smc.getValue())
							.mapToKeyValue(member -> KeyValue.of(member.getIdShort(), ElementValues.getValue(member)))
							.toMap();
		return new ElementCollectionValue(members);
	}
	
	public static ElementCollectionValue fromValueObject(Object obj, SubmodelElementCollection smc)
		throws IOException {
		if ( obj instanceof Map vmap ) {
			Map<String,ElementValue> smevMap
						= FStream.from(smc.getValue())
								.mapToKeyValueOrThrow(member -> {
									Object fieldValue = vmap.get(member.getIdShort());
									ElementValue elmVal = (fieldValue != null)
			                                                ? ElementValues.fromValueObject(fieldValue, member)
			                                                : null;
									return KeyValue.of(member.getIdShort(), elmVal);
								})
								.toMap();
			return new ElementCollectionValue(smevMap);
		}
		else {
			throw new IOException("ElementCollectionValue value is not Map: obj=" + obj);
		}
	}
	
	public static ElementCollectionValue parseValueJsonNode(JsonNode vnode, SubmodelElementCollection smc)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("ElementCollectionValue expects an 'Object' node: JsonNode=" + vnode);
		}
		
		var valueFields
			= FStream.from(smc.getValue())
					.mapToKeyValueOrThrow(member -> {
						JsonNode field = JacksonUtils.getFieldOrNull(vnode, member.getIdShort());
						ElementValue fieldVal = (field != null) ? ElementValues.parseValueJsonNode(field, member) : null;
						return KeyValue.of(member.getIdShort(), fieldVal);
					})
					.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(valueFields);
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
