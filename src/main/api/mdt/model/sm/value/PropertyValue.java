package mdt.model.sm.value;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import mdt.aas.DataTypes;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class PropertyValue implements DataElementValue, Supplier<String> {
	public static final String SERIALIZATION_TYPE = "mdt:value:property";
	
	private final String m_value;
	
	public PropertyValue(String value) {
		m_value = value;
	}

	@Override
	public String get() {
		return m_value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || PropertyValue.class != obj.getClass() ) {
			return false;
		}
		
		PropertyValue other = (PropertyValue) obj;
		return Objects.equals(m_value, other.m_value);
	}
	
	@Override
	public String toString() {
		return "" + m_value;
	}
	
	public static PropertyValue parseJsonNode(JsonNode jnode) throws IOException {
		return new PropertyValue(jnode.asText());
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeString(m_value);
	}
	
	public static PropertyValue DATE_TIME(Instant ts) {
		return new PropertyValue(DataTypes.DATE_TIME.toValueString(ts));
	}
}
