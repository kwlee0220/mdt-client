package mdt.model.sm.value;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.json.JacksonUtils;

import mdt.aas.DataType;
import mdt.aas.DataTypes;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RangeValue<T> extends AbstractElementValue implements DataElementValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:range";
	
	private final DataType<T> m_vtype;
	private final T m_min;
	private final T m_max;
	
	public RangeValue(DataType<T> vtype, T min, T max) {
		m_vtype = vtype;
		m_min = min;
		m_max = max;
	}
	
	public DataType<?> getValueType() {
		return m_vtype;
	}
	
	/**
	 * 이 값의 min/max를 주어진 {@link Range}에 값 문자열로 설정한다.
	 * <p>
	 * 타입 규칙은 {@link PropertyValue#update(org.eclipse.digitaltwin.aas4j.v3.model.Property)}와 같다:
	 * Range의 {@code valueType}과 같은 타입이면 그대로, {@code xs:string} Range에는 값 문자열로 쓰고,
	 * 숫자끼리는 손실 없는 변환만 허용하며 그 밖의 불일치는 거부한다. min/max를 모두 변환한 뒤에 기록하므로
	 * 실패하면 Range는 바뀌지 않는다. 2026-11-01 이전에는 타입을 확인하지 않아 {@code xs:int} Range에
	 * {@code "abc"}가 그대로 기록됐다.
	 *
	 * @param rg	값을 설정할 Range.
	 * @throws IllegalArgumentException	Range의 데이터 타입과 맞지 않는 값인 경우.
	 */
	public void update(Range rg) {
		String element = String.format("range '%s'", rg.getIdShort());
		String min = PropertyValue.toValueStringFor(m_vtype, m_min, rg.getValueType(), element);
		String max = PropertyValue.toValueStringFor(m_vtype, m_max, rg.getValueType(), element);
		rg.setMin(min);
		rg.setMax(max);
	}
	
	@Override
	public Map<String,T> toValueObject() {
		Map<String, T> value = Maps.newLinkedHashMap();
		value.put(FIELD_MIN, m_min);
		value.put(FIELD_MAX, m_max);

		return value;
	}
	
	public T getMin() {
		return m_min;
	}
	
	public T getMax() {
		return m_max;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RangeValue<?> from(Range rg) {
		DataType<?> vtype = DataTypes.fromAas4jDatatype(rg.getValueType());
		Object min = vtype.parseValueString(rg.getMin());
		Object max = vtype.parseValueString(rg.getMax());

		return new RangeValue(vtype, min, max);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RangeValue<?> fromValueObject(Object value, DataTypeDefXsd vtypeXsd) throws IOException {
		if ( value instanceof Map vmap ) {
			DataType<?> vtype = DataTypes.fromAas4jDatatype(vtypeXsd);
			Object min = vtype.fromJdbcObject(vmap.get(FIELD_MIN));
			Object max = vtype.fromJdbcObject(vmap.get(FIELD_MAX));

			return new RangeValue(vtype, min, max);
		}
		else {
			throw new IOException("invalid RangeValue object: " + value);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RangeValue<?> parseValueJsonNode(JsonNode vnode, DataTypeDefXsd vtypeXsd)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("RangeValue expects an 'Object' node: JsonNode=" + vnode);
		}

		DataType<?> vtype = DataTypes.fromAas4jDatatype(vtypeXsd);
		Object min = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MIN));
		Object max = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MAX));
				
		return new RangeValue(vtype, min, max);
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
		return Objects.equals(m_vtype, other.m_vtype)
				&& Objects.equals(m_min, other.m_min)
				&& Objects.equals(m_max, other.m_max);
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_vtype, m_min, m_max);
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
	public static RangeValue<?> deserializeValue(JsonNode vnode) throws IOException {
		DataType<?> vtype = DataTypes.fromDataTypeName(JacksonUtils.getStringField(vnode, FIELD_VTYPE));
		Object min = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MIN));
		Object max = vtype.fromJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_MAX));
				
		return new RangeValue(vtype, min, max);
	}
}
