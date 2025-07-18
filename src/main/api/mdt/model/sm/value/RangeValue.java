package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.json.JacksonUtils;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RangeValue<T> extends AbstractElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:range";
	
	@NonNull private final DataType<T> m_vtype;
	private final T m_min;
	private final T m_max;
	
	public RangeValue(@NonNull DataType<T> vtype, T min, T max) {
		m_vtype = vtype;
		m_min = min;
		m_max = max;
	}
	
	public DataType<?> getValueType() {
		return m_vtype;
	}
	
	public T getMin() {
		return m_min;
	}
	
	public T getMax() {
		return m_max;
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}
	
	public static RangeValue<?> parseValueJsonNode(Range range, JsonNode vnode) {
		DataType<?> vtype = DataTypes.fromAas4jDatatype(range.getValueType());
		Object min = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MIN));
		Object max = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MAX));
				
		return new RangeValue(vtype, min, max);
	}

	@Override
	public Object toValueJsonObject() {
		Map<String,Object> value = Maps.newLinkedHashMap();
		value.put(FIELD_MIN, m_vtype.toJdbcObject(m_min));
		value.put(FIELD_MAX, m_vtype.toJdbcObject(m_max));
		
		return value;
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

		@SuppressWarnings("rawtypes")
		RangeValue other = (RangeValue) obj;
		return m_min.equals(other.m_min)
				&& m_max.equals(other.m_max);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_min, m_max);
	}

	private static final String FIELD_VTYPE = "vtype";
	private static final String FIELD_MIN = "min";
	private static final String FIELD_MAX = "max";
	
	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_VTYPE, m_vtype.getName());
		gen.writeObjectField(FIELD_MIN, m_vtype.toJsonObject(m_min));
		gen.writeObjectField(FIELD_MAX, m_vtype.toJsonObject(m_max));
		gen.writeEndObject();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RangeValue<?> deserializeValue(JsonNode vnode) {
		DataType<?> vtype = DataTypes.fromDataTypeName(JacksonUtils.getStringField(vnode, FIELD_VTYPE));
		Object min = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MIN));
		Object max = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MAX));
				
		return new RangeValue(vtype, min, max);
	}
}
