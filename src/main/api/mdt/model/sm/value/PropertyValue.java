package mdt.model.sm.value;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import mdt.aas.DataType;
import mdt.aas.DataTypes;


/**
 * AAS {@link Property}의 타입별 값을 표현하는 {@link ElementValue}.
 * <p>
 * XSD 데이터 타입별로 구체 하위 클래스({@link StringPropertyValue}, {@link IntegerPropertyValue},
 * {@link FloatPropertyValue}, {@link DoublePropertyValue}, {@link BooleanPropertyValue},
 * {@link DateTimePropertyValue}, {@link DurationPropertyValue}, {@link LongPropertyValue},
 * {@link ShortPropertyValue}, {@link DecimalPropertyValue})가 정의되어 있으며, 각 타입에 대응하는
 * 정적 팩토리({@link #STRING(String)}, {@link #INTEGER(Integer)} 등)를 제공한다.
 * 값과 문자열/JSON 사이의 변환은 {@link DataType}({@link #getDataType()})이 담당한다.
 * <p>
 * {@link Property}나 그 값으로부터 알맞은 하위 타입을 생성하려면 {@link #from(Property)},
 * {@link #fromValueObject(Object, Property)}, {@link #parseValueJsonNode(JsonNode, Property)}를
 * 사용한다.
 *
 * @param <T>	값의 Java 타입.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class PropertyValue<T> extends AbstractElementValue implements DataElementValue {
	protected final T m_value;

	/**
	 * 이 값의 데이터 타입을 반환한다.
	 *
	 * @return 데이터 타입.
	 */
	abstract public DataType<T> getDataType();

	/**
	 * 주어진 값으로 PropertyValue를 생성한다.
	 *
	 * @param value	값({@code null} 허용).
	 */
	protected PropertyValue(T value) {
		m_value = value;
	}

	/**
	 * 이 값을 주어진 {@link Property}에 값 문자열로 설정한다.
	 * <p>
	 * 값이 {@code null}이면 Property의 값도 {@code null}로 설정한다.
	 *
	 * @param prop	값을 설정할 Property.
	 */
	public void update(Property prop) {
		prop.setValue((m_value != null) ? getDataType().toValueString(m_value) : null);
	}

	@Override
	public String toDisplayString() {
		return getDataType().toValueString(m_value);
	}
	
	/**
	 * Java 값 객체와 대상 {@link Property}의 데이터 타입으로부터 알맞은 {@code PropertyValue} 하위 타입을 생성한다.
	 *
	 * @param v		값 객체. Property의 valueType에 해당하는 Java 타입이어야 한다.
	 * @param prop	데이터 타입 결정에 사용할 대상 Property.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입인 경우.
	 */
	public static PropertyValue<?> fromValueObject(Object v, Property prop) {
		switch ( prop.getValueType() ) {
			case STRING:
				return new StringPropertyValue((String)v);
			case INT:
				return new IntegerPropertyValue((Integer)v);
			case FLOAT:
				return new FloatPropertyValue((Float)v);
			case DOUBLE:
				return new DoublePropertyValue((Double)v);
			case BOOLEAN:
				return new BooleanPropertyValue((Boolean)v);
			case DATE_TIME:
				return new DateTimePropertyValue((Instant)v);
			case DURATION:
				return new DurationPropertyValue((Duration)v);
			case LONG:
				return new LongPropertyValue((Long)v);
			case SHORT:
				return new ShortPropertyValue((Short)v);
			case DECIMAL:
				return new DecimalPropertyValue((BigDecimal)v);
			default:
				throw new IllegalArgumentException("unknown data type: " + prop.getValueType());
		}
	}
	
	/**
	 * JSON 노드와 대상 {@link Property}의 데이터 타입으로부터 알맞은 {@code PropertyValue} 하위 타입을 생성한다.
	 *
	 * @param vnode	값에 해당하는 JSON 노드.
	 * @param prop	데이터 타입 결정에 사용할 대상 Property.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입인 경우.
	 */
	public static PropertyValue<?> parseValueJsonNode(JsonNode vnode, Property prop) {
		switch ( prop.getValueType() ) {
			case STRING:
				return StringPropertyValue.deserializeValue(vnode);
			case INT:
				return IntegerPropertyValue.deserializeValue(vnode);
			case FLOAT:
				return FloatPropertyValue.deserializeValue(vnode);
			case DOUBLE:
				return DoublePropertyValue.deserializeValue(vnode);
			case BOOLEAN:
				return BooleanPropertyValue.deserializeValue(vnode);
			case DATE_TIME:
				return DateTimePropertyValue.deserializeValue(vnode);
			case DURATION:
				return DurationPropertyValue.deserializeValue(vnode);
			case LONG:
				return LongPropertyValue.deserializeValue(vnode);
			case SHORT:
				return ShortPropertyValue.deserializeValue(vnode);
			case DECIMAL:
				return DecimalPropertyValue.deserializeValue(vnode);
			default:
				throw new IllegalArgumentException("unknown data type: " + prop.getValueType());
		}
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeObject(toValueObject());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_value, getDataType());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		PropertyValue<?> other = (PropertyValue<?>) obj;
		return Objects.equals(m_value, other.m_value)
				&& Objects.equals(getDataType(), other.getDataType());
	}
	
	/**
	 * 주어진 {@link Property}의 값 문자열을 데이터 타입에 맞게 파싱하여 {@code PropertyValue}를 생성한다.
	 *
	 * @param prop	값과 데이터 타입을 가진 Property.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입인 경우.
	 */
	public static PropertyValue<?> from(Property prop) {
		String value = prop.getValue();
		switch ( prop.getValueType() ) {
			case STRING:
				return STRING(value);
			case INT:
				return INTEGER(DataTypes.INT.parseValueString(value));
			case FLOAT:
				return FLOAT(DataTypes.FLOAT.parseValueString(value));
			case DOUBLE:
				return DOUBLE(DataTypes.DOUBLE.parseValueString(value));
			case BOOLEAN:
				return BOOLEAN(DataTypes.BOOLEAN.parseValueString(value));
			case DATE_TIME:
				return DATE_TIME(DataTypes.DATE_TIME.parseValueString(value));
			case DURATION:
				return DURATION(DataTypes.DURATION.parseValueString(value));
			case LONG:
				return LONG(DataTypes.LONG.parseValueString(value));
			case SHORT:
				return SHORT(DataTypes.SHORT.parseValueString(value));
			case DECIMAL:
				return DECIMAL(DataTypes.DECIMAL.parseValueString(value));
			default:
				throw new IllegalArgumentException("unknown data type: " + prop.getValueType());
		}
	}
	
	public static StringPropertyValue STRING(@Nullable String value) {
		return new StringPropertyValue(value);
	}
	public static class StringPropertyValue extends PropertyValue<String> {
		public static final String SERIALIZATION_TYPE = "mdt:value:string";
		
		public StringPropertyValue(String value) {
			super(value);
		}

		@Override
		public DataType<String> getDataType() {
			return DataTypes.STRING;
		}
		
		@Override
		public String toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static StringPropertyValue deserializeValue(JsonNode vnode) {
			return new StringPropertyValue(DataTypes.STRING.fromJsonNode(vnode));
		}
	}

	public static IntegerPropertyValue INTEGER(Integer value) {
		return new IntegerPropertyValue(value);
	}
	public static class IntegerPropertyValue extends PropertyValue<Integer> {
		public static final String SERIALIZATION_TYPE = "mdt:value:integer";
		
		public IntegerPropertyValue(Integer value) {
			super(value);
		}

		@Override
		public DataType<Integer> getDataType() {
			return DataTypes.INT;
		}
		
		@Override
		public Integer toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static IntegerPropertyValue deserializeValue(JsonNode vnode) {
			return new IntegerPropertyValue(DataTypes.INT.fromJsonNode(vnode));
		}
	}

	public static FloatPropertyValue FLOAT(Float value) {
		return new FloatPropertyValue(value);
	}
	public static class FloatPropertyValue extends PropertyValue<Float> {
		public static final String SERIALIZATION_TYPE = "mdt:value:float";
		
		public FloatPropertyValue(Float value) {
			super(value);
		}

		@Override
		public DataType<Float> getDataType() {
			return DataTypes.FLOAT;
		}
		
		@Override
		public Float toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static FloatPropertyValue deserializeValue(JsonNode vnode) {
			return new FloatPropertyValue(DataTypes.FLOAT.fromJsonNode(vnode));
		}
	}

	public static DoublePropertyValue DOUBLE(Double value) {
		return new DoublePropertyValue(value);
	}
	public static class DoublePropertyValue extends PropertyValue<Double> {
		public static final String SERIALIZATION_TYPE = "mdt:value:double";
		
		public DoublePropertyValue(Double value) {
			super(value);
		}

		@Override
		public DataType<Double> getDataType() {
			return DataTypes.DOUBLE;
		}
		
		@Override
		public Double toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DoublePropertyValue deserializeValue(JsonNode vnode) {
			return new DoublePropertyValue(DataTypes.DOUBLE.fromJsonNode(vnode));
		}
	}

	public static BooleanPropertyValue BOOLEAN(Boolean value) {
		return new BooleanPropertyValue(value);
	}
	public static class BooleanPropertyValue extends PropertyValue<Boolean> {
		public static final String SERIALIZATION_TYPE = "mdt:value:boolean";
		
		public BooleanPropertyValue(Boolean value) {
			super(value);
		}

		@Override
		public DataType<Boolean> getDataType() {
			return DataTypes.BOOLEAN;
		}
		
		@Override
		public Boolean toValueObject() {
			return m_value;
		}
		
		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static BooleanPropertyValue deserializeValue(JsonNode vnode) {
			return new BooleanPropertyValue(DataTypes.BOOLEAN.fromJsonNode(vnode));
		}
	}

	public static DateTimePropertyValue DATE_TIME(Instant value) {
		return new DateTimePropertyValue(value);
	}
	public static class DateTimePropertyValue extends PropertyValue<Instant> {
		public static final String SERIALIZATION_TYPE = "mdt:value:dateTime";
		
		public DateTimePropertyValue(Instant value) {
			super(value);
		}

		@Override
		public DataType<Instant> getDataType() {
			return DataTypes.DATE_TIME;
		}
		
		@Override
		public Instant toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DateTimePropertyValue deserializeValue(JsonNode vnode) {
			return new DateTimePropertyValue(DataTypes.DATE_TIME.fromJsonNode(vnode));
		}
	}

	public static DurationPropertyValue DURATION(Duration value) {
		return new DurationPropertyValue(value);
	}
	/**
	 * XSD {@code xs:duration} 타입 Property 값.
	 * <p>
	 * 다른 타입과 달리 값을 ISO-8601 duration 문자열로 직렬화한다
	 * ({@link #toValueJsonString()}/{@link #serializeValue(JsonGenerator)} 재정의).
	 */
	public static class DurationPropertyValue extends PropertyValue<Duration> {
		public static final String SERIALIZATION_TYPE = "mdt:value:duration";
		
		public DurationPropertyValue(Duration value) {
			super(value);
		}

		@Override
		public DataType<Duration> getDataType() {
			return DataTypes.DURATION;
		}
		
		@Override
		public Duration toValueObject() {
			return m_value;
		}

		@Override
		public String toValueJsonString() {
			return m_value != null ? "\"" + DataTypes.DURATION.toValueString(m_value) + "\"" : "null";
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeValue(JsonGenerator gen) throws IOException {
			String str = (m_value != null) ? m_value.toString() : null;
			gen.writeObject(str);
		}
		
		public static DurationPropertyValue deserializeValue(JsonNode vnode) {
			return new DurationPropertyValue(DataTypes.DURATION.fromJsonNode(vnode));
		}
	}

	public static LongPropertyValue LONG(Long value) {
		return new LongPropertyValue(value);
	}
	public static class LongPropertyValue extends PropertyValue<Long> {
		public static final String SERIALIZATION_TYPE = "mdt:value:long";
		
		public LongPropertyValue(Long value) {
			super(value);
		}

		@Override
		public DataType<Long> getDataType() {
			return DataTypes.LONG;
		}
		
		@Override
		public Long toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static LongPropertyValue deserializeValue(JsonNode vnode) {
			return new LongPropertyValue(DataTypes.LONG.fromJsonNode(vnode));
		}
	}

	public static ShortPropertyValue SHORT(Short value) {
		return new ShortPropertyValue(value);
	}
	public static class ShortPropertyValue extends PropertyValue<Short> {
		public static final String SERIALIZATION_TYPE = "mdt:value:short";
		
		public ShortPropertyValue(Short value) {
			super(value);
		}

		@Override
		public DataType<Short> getDataType() {
			return DataTypes.SHORT;
		}
		
		@Override
		public Short toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static ShortPropertyValue deserializeValue(JsonNode vnode) {
			return new ShortPropertyValue(DataTypes.SHORT.fromJsonNode(vnode));
		}
	}

	public static DecimalPropertyValue DECIMAL(BigDecimal value) {
		return new DecimalPropertyValue(value);
	}
	public static class DecimalPropertyValue extends PropertyValue<BigDecimal> {
		public static final String SERIALIZATION_TYPE = "mdt:value:decimal";
		
		public DecimalPropertyValue(BigDecimal value) {
			super(value);
		}

		@Override
		public DataType<BigDecimal> getDataType() {
			return DataTypes.DECIMAL;
		}
		
		@Override
		public BigDecimal toValueObject() {
			return m_value;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DecimalPropertyValue deserializeValue(JsonNode vnode) {
			return new DecimalPropertyValue(DataTypes.DECIMAL.fromJsonNode(vnode));
		}
	}
}
