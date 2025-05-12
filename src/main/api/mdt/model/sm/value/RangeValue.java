package mdt.model.sm.value;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RangeValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:range";
	
	private final String m_min;
	private final String m_max;
	
	public RangeValue(String min, String max) {
		m_min = min;
		m_max = max;
	}
	
	public String getMin() {
		return m_min;
	}
	
	public String getMax() {
		return m_max;
	}
	
	@Override
	public String toString() {
		return String.format("['%s', '%s']", this.m_min, this.m_max);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || RangeValue.class != obj.getClass() ) {
			return false;
		}

		RangeValue other = (RangeValue) obj;
		return m_min.equals(other.m_min)
				&& m_max.equals(other.m_max);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_min, m_max);
	}

	private static final String FIELD_MIN = "min";
	private static final String FIELD_MAX = "max";
	
	public static RangeValue parseJsonNode(JsonNode jnode) {
		String min = JacksonUtils.getStringField(jnode, FIELD_MIN);
		String max = JacksonUtils.getStringField(jnode, FIELD_MAX);
		
		return new RangeValue(min, max);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_MIN, this.m_min);
		gen.writeStringField(FIELD_MAX, this.m_max);
		gen.writeEndObject();
	}
}
