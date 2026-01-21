package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.stream.FStream;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementListValue extends AbstractElementValue implements ElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:list";
	
	private final List<ElementValue> m_elementValues;
	
	public ElementListValue(List<ElementValue> values) {
		m_elementValues = values;
	}
	
	public List<ElementValue> getElementAll() {
		return m_elementValues;
	}

	@Override
	public List<Object> toValueObject() {
		return FStream.from(m_elementValues)
						.map(ElementValue::toValueObject)
						.toList();
	}
	
	public void update(SubmodelElementList sml) {
		FStream.from(sml.getValue())
				.zipWith(FStream.from(m_elementValues))
				.forEach(match -> ElementValues.update(match._1, match._2));
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}
	
	public static ElementListValue from(SubmodelElementList sml) {
		List<ElementValue> values = FStream.from(sml.getValue())
											.mapOrThrow(ElementValues::getValue)
											.toList();
		return new ElementListValue(values);
	}
	
	public static ElementListValue fromValueObject(Object obj, SubmodelElementList sml)
		throws IOException {
		if ( obj instanceof Iterable iter ) {
			List<ElementValue> members
					= FStream.from(sml.getValue())
							.zipWith(FStream.<Object>from(iter))
							.mapOrThrow(pair -> ElementValues.fromValueObject(pair._2, pair._1))
		                    .toList();
			return new ElementListValue(members);
		}
		else {
			throw new IOException("Invalid object for ElementListValue: " + obj);
		}
	}
	
	public static ElementListValue parseValueJsonNode(JsonNode vnode, SubmodelElementList sml) throws IOException {
		if ( !vnode.isArray() ) {
			throw new IOException("ElementListValue expects an 'Array' node: JsonNode=" + vnode);
		}
		List<ElementValue> values = FStream.from(sml.getValue())
											.zipWith(FStream.from(vnode.elements()))
											.mapOrThrow(pair -> ElementValues.parseValueJsonNode(pair._2, pair._1))
											.toList();
		return new ElementListValue(values);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_elementValues);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || ElementCollectionValue.class != obj.getClass() ) {
			return false;
		}
		
		ElementListValue other = (ElementListValue) obj;
		return Objects.equals(m_elementValues, other.m_elementValues);
	}
	
	@Override
	public String toString() {
		return m_elementValues.toString();
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartArray();
		for ( ElementValue smev: m_elementValues ) {
			gen.writeObject(smev);
		}
		gen.writeEndArray();
	}
	
	public static ElementListValue deserializeValue(JsonNode vnode) {
		List<ElementValue> elements = FStream.from(vnode.elements())
											.mapOrThrow(elmNode -> ElementValues.parseJsonNode(elmNode))
											.toList();
		return new ElementListValue(elements);
	}
}
