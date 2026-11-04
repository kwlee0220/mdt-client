package mdt.model.sm.value;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.DataUtils;
import utils.json.JacksonDeserializationException;
import utils.json.JsonScalars;

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
 * 알맞은 하위 타입을 {@link Property}로부터 생성하려면 {@link #from(Property)}을, 값 객체로부터 생성하려면
 * 데이터 타입을 받는 {@link #fromValueObject(Object, DataTypeDefXsd)}를, 값 JSON으로부터 생성하려면 템플릿을 받는
 * {@link #parseValueJsonNode(JsonNode, PropertyValue)}를 사용한다.
 *
 * @param <T>	값의 Java 타입.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract sealed class PropertyValue<T> extends AbstractElementValue implements DataElementValue {
	/** Property 값. {@code null}이면 값이 설정되지 않았음을 의미한다. */
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
	 * Property의 {@code valueType}과 이 값의 데이터 타입이 같아야 한다. 다르면 숫자끼리의 <b>손실 없는</b> 변환만
	 * 허용한다(예: {@code xs:int} 값 → {@code xs:long}·{@code xs:integer}·{@code xs:double} 속성, 3.0 →
	 * {@code xs:int}). {@code xs:string} 속성에는 어떤 값이든 그 값 문자열로 기록한다.
	 * 그 밖의 불일치(문자열 값 → 정수 속성, 3.7 → {@code xs:int}, 범위 초과 등)는 거부한다.
	 * {@code valueType}이 없는 Property와 {@code null} 값은 검사하지 않는다.
	 *
	 * @param prop	값을 설정할 Property.
	 * @throws IllegalArgumentException	Property의 데이터 타입과 맞지 않는 값인 경우.
	 */
	public void update(Property prop) {
		prop.setValue(toValueStringFor(prop));
	}

	private String toValueStringFor(Property prop) {
		return toValueStringFor(getDataType(), m_value, prop.getValueType(),
								String.format("property '%s'", prop.getIdShort()));
	}

	/**
	 * 데이터 타입이 {@code dtype}인 값을 데이터 타입이 {@code target}인 원소에 기록할 값 문자열로 바꾼다.
	 * {@link #update(Property)}와 {@link RangeValue#update}가 공유하는 규칙이다.
	 * <ul>
	 *   <li>{@code null} 값 → {@code null}.</li>
	 *   <li>같은 타입이거나 {@code target}이 {@code null} → 그대로.</li>
	 *   <li>{@code target}이 {@code xs:string} → 값 문자열(손실 없음).</li>
	 *   <li>숫자 값 → 숫자 {@code target}: 손실 없는 변환만 허용.</li>
	 *   <li>그 밖 → {@link IllegalArgumentException}.</li>
	 * </ul>
	 *
	 * @param element	오류 메시지에 쓸 원소 기술(예: {@code "property 'p'"}).
	 */
	@SuppressWarnings("unchecked")
	static String toValueStringFor(DataType<?> dtype, @Nullable Object value, @Nullable DataTypeDefXsd target,
									String element) {
		if ( value == null ) {
			return null;
		}
		DataType<Object> vtype = (DataType<Object>)dtype;
		if ( target == null || target == vtype.getTypeDefXsd() ) {
			return vtype.toValueString(value);
		}
		if ( target == DataTypeDefXsd.STRING ) {
			// 어떤 값이든 문자열 속성에는 그 값 문자열로 기록한다(손실 없음). 예: OPC UA에서 읽은 double을 문자열 속성에.
			return vtype.toValueString(value);
		}
		if ( value instanceof Number number && isNumericType(target) ) {
			return toNumericValueString(number, target, element);
		}
		throw new IllegalArgumentException(String.format("value type mismatch: %s is %s, but value is %s",
														element, target, vtype.getTypeDefXsd()));
	}

	/**
	 * 주어진 문자열을 Property의 {@code valueType}으로 해석할 수 있는지 확인하고, 그 문자열을 그대로 돌려준다.
	 * <p>
	 * 해석 결과로 다시 만든 문자열이 아니라 원래 문자열을 쓴다({@code xs:dateTime}의 시간대 표기 등을 바꾸지 않기 위해).
	 * {@link DataTypes}에 없는 정수 타입({@code xs:integer}, {@code xs:byte})은 다른 정수 타입처럼 정수 문법과 범위를
	 * 확인한다({@code "3.0"}, 앞뒤 공백, 지수 표기는 거부, 빈 문자열은 허용). 그 밖에
	 * {@link DataTypes}에 없는 타입과 {@code valueType}이 없는 Property는 확인하지 않는다.
	 *
	 * @param prop	값을 설정할 Property.
	 * @param str	값 문자열({@code null}이면 {@code null}).
	 * @return	{@code str}.
	 * @throws IllegalArgumentException	{@code str}을 Property의 데이터 타입으로 해석할 수 없는 경우.
	 */
	public static String checkValueString(Property prop, @Nullable String str) {
		DataTypeDefXsd target = prop.getValueType();
		if ( str == null || target == null ) {
			return str;
		}
		DataType<?> dtype = DataTypes.fromAas4jDatatype(target);
		try {
			if ( dtype != null ) {
				dtype.parseValueString(str);
			}
			else if ( isNumericType(target) && !str.isEmpty() ) {
				// xs:integer·xs:byte. 다른 정수 타입(Integer.parseInt 등)과 같이 정수 문법만 받는다:
				// "3.0"·" 3 "·"1e2"는 거부한다(BigInteger 문법). 범위는 toNumericValueString이 확인한다.
				toNumericValueString(new java.math.BigInteger(str), target,
										String.format("property '%s'", prop.getIdShort()));
			}
			return str;
		}
		catch ( IllegalArgumentException | java.time.DateTimeException e ) {
			throw new IllegalArgumentException(String.format("invalid %s value for property '%s'", target,
															prop.getIdShort()), e);
		}
	}

	// 손실 없는 변환을 허용하는 숫자 데이터 타입. JsonScalars가 지원하는 범위와 같다.
	private static boolean isNumericType(DataTypeDefXsd type) {
		return switch ( type ) {
			case BYTE, SHORT, INT, LONG, INTEGER, FLOAT, DOUBLE, DECIMAL -> true;
			default -> false;
		};
	}

	/**
	 * 숫자를 대상 숫자 타입의 값 문자열로 손실 없이 바꾼다. 범위를 넘거나 정수 타입에 소수부가 있으면 거부한다.
	 * 규칙은 {@link JsonScalars}({@link JsonScalars.Policy#TEXT_TOLERANT})를 따른다.
	 */
	private static String toNumericValueString(Number number, DataTypeDefXsd target, String element) {
		JsonNode node = toNumberNode(number);
		JsonScalars.Policy policy = JsonScalars.Policy.TEXT_TOLERANT;
		try {
			return switch ( target ) {
				case BYTE -> Byte.toString(JsonScalars.toByte(node, policy));
				case SHORT -> DataTypes.SHORT.toValueString(JsonScalars.toShort(node, policy));
				case INT -> DataTypes.INT.toValueString(JsonScalars.toInt(node, policy));
				case LONG -> DataTypes.LONG.toValueString(JsonScalars.toLong(node, policy));
				case INTEGER -> JsonScalars.toBigInteger(node, policy).toString();
				case FLOAT -> DataTypes.FLOAT.toValueString(JsonScalars.toFloat(node, policy));
				case DOUBLE -> DataTypes.DOUBLE.toValueString(JsonScalars.toDouble(node, policy));
				case DECIMAL -> DataTypes.DECIMAL.toValueString(JsonScalars.toDecimal(node, policy));
				default -> throw new IllegalArgumentException("not a numeric type: " + target);
			};
		}
		catch ( JacksonDeserializationException e ) {
			throw new IllegalArgumentException(String.format("value cannot be converted to %s without loss: %s, cause=%s",
															target, element, e.getMessage()), e);
		}
	}

	private static JsonNode toNumberNode(Number number) {
		JsonNodeFactory factory = JsonNodeFactory.instance;
		return switch ( number ) {
			case BigDecimal bd -> factory.numberNode(bd);
			case java.math.BigInteger bi -> factory.numberNode(bi);
			case Double d -> factory.numberNode(d);
			case Float f -> factory.numberNode(f);
			case Long l -> factory.numberNode(l);
			case Integer i -> factory.numberNode(i);
			case Short sh -> factory.numberNode(sh);
			case Byte b -> factory.numberNode(b);
			default -> factory.numberNode(new BigDecimal(number.toString()));
		};
	}
	
	/**
	 * 이 값의 데이터 타입과 값 문자열이 설정된 {@link DefaultProperty.Builder}를 반환한다.
	 * <p>
	 * idShort 등 나머지 속성은 호출자가 설정해야 한다. 값이 {@code null}이면 값 문자열도 {@code null}이다.
	 *
	 * @return	Property 빌더.
	 */
	public DefaultProperty.Builder toElementBuilder() {
		DefaultProperty.Builder builder = new DefaultProperty.Builder();
		builder.valueType(getDataType().getTypeDefXsd());
		builder.value((m_value != null) ? getDataType().toValueString(m_value) : null);
		return builder;
	}

	@Override
	public String toDisplayString() {
		return getDataType().toValueString(m_value);
	}
	
	/**
	 * Java 값 객체와 대상 {@link Property}의 데이터 타입으로부터 알맞은 {@code PropertyValue} 하위 타입을 생성한다.
	 * <p>
	 * 값 객체의 타입이 데이터 타입에 정확히 일치하지 않아도 가능한 경우 관대하게 변환한다
	 * (예: LONG 타입에 {@link Integer} 값, 숫자 타입에 숫자 문자열).
	 *
	 * @param v		값 객체({@code null} 허용).
	 * @param xsdType	데이터 타입 결정에 사용할 대상 Property의 XSD 데이터 타입.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입이거나 값 객체를 변환할 수 없는 경우.
	 */
	public static PropertyValue<?> fromValueObject(Object v, DataTypeDefXsd xsdType) {
		switch ( xsdType ) {
			case STRING:
				return STRING(DataUtils.asString(v));
			case INT:
				return INTEGER((v != null) ? DataUtils.asInt(v) : null);
			case FLOAT:
				return FLOAT((v != null) ? DataUtils.asFloat(v) : null);
			case DOUBLE:
				return DOUBLE((v != null) ? DataUtils.asDouble(v) : null);
			case BOOLEAN:
				return BOOLEAN((v != null) ? DataUtils.asBoolean(v) : null);
			case DATE_TIME:
				return DATE_TIME((v != null) ? DataUtils.asInstant(v) : null);
			case DURATION:
				return DURATION(asDuration(v));
			case LONG:
				return LONG((v != null) ? DataUtils.asLong(v) : null);
			case SHORT:
				return SHORT((v != null) ? DataUtils.asShort(v) : null);
			case DECIMAL:
				return DECIMAL(asDecimal(v));
			default:
				throw new IllegalArgumentException("unknown data type: " + xsdType);
		}
	}

	private static Duration asDuration(Object v) {
		if ( v == null ) {
			return null;
		}
		else if ( v instanceof Duration dur ) {
			return dur;
		}
		else {
			return DataTypes.DURATION.parseValueString(v.toString());
		}
	}

	private static BigDecimal asDecimal(Object v) {
		if ( v == null ) {
			return null;
		}
		else if ( v instanceof BigDecimal decimal ) {
			return decimal;
		}
		else {
			return DataTypes.DECIMAL.parseValueString(v.toString());
		}
	}
	
	/**
	 * JSON 노드와 대상 {@link Property}의 데이터 타입으로부터 알맞은 {@code PropertyValue} 하위 타입을 생성한다.
	 *
	 * <p>
	 * {@link #parseValueJsonNode(JsonNode, DataTypeDefXsd)}에 {@code prop}의 XSD 데이터 타입을 넘긴다.
	 *
	 * @param vnode	값에 해당하는 JSON 노드.
	 * @param prop	데이터 타입 결정에 사용할 템플릿 PropertyValue.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입인 경우.
	 * @throws IOException	JSON 노드를 해당 데이터 타입의 값으로 해석할 수 없는 경우.
	 */
	public static PropertyValue<?> parseValueJsonNode(JsonNode vnode, PropertyValue<?> prop) throws IOException {
		return parseValueJsonNode(vnode, prop.getDataType().getTypeDefXsd());
	}
	/**
	 * JSON 노드를 주어진 XSD 데이터 타입의 값으로 해석하여 알맞은 {@code PropertyValue} 하위 타입을 생성한다.
	 * <p>
	 * 해석은 각 하위 타입의 {@code deserializeValue}(즉 {@link DataType#fromJsonNode(JsonNode)})가 담당한다.
	 * 지원하는 데이터 타입은 {@code xs:string}, {@code xs:int}, {@code xs:float}, {@code xs:double},
	 * {@code xs:boolean}, {@code xs:dateTime}, {@code xs:duration}, {@code xs:long}, {@code xs:short},
	 * {@code xs:decimal}이다.
	 *
	 * @param vnode		값에 해당하는 JSON 노드.
	 * @param xsdType	값의 XSD 데이터 타입.
	 * @return 생성된 PropertyValue.
	 * @throws IllegalArgumentException	지원하지 않는 데이터 타입인 경우.
	 * @throws IOException	JSON 노드를 해당 데이터 타입의 값으로 해석할 수 없는 경우.
	 */
	public static PropertyValue<?> parseValueJsonNode(JsonNode vnode, DataTypeDefXsd xsdType) throws IOException {
		switch ( xsdType ) {
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
				throw new IllegalArgumentException("unknown data type: " + xsdType);
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
	 * <p>
	 * {@code xs:integer}는 대응하는 {@code PropertyValue}가 없으므로 {@link LongPropertyValue}({@code xs:long})로
	 * 생성한다. 따라서 long 범위를 넘는 값은 파싱하지 못한다.
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
			case INTEGER:
				// xs:integer는 범위 제한이 없는 정수지만 대응하는 PropertyValue가 없으므로, int보다 넓은 long으로 읽는다.
				return LONG(DataTypes.LONG.parseValueString(value));
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
	
	/**
	 * 주어진 값을 갖는 {@code xs:string} 타입의 {@link StringPropertyValue}를 생성한다.
	 *
	 * @param value	문자열 값({@code null} 허용).
	 * @return	생성된 {@link StringPropertyValue}.
	 */
	public static StringPropertyValue STRING(@Nullable String value) {
		return new StringPropertyValue(value);
	}
	/**
	 * XSD {@code xs:string} 타입 Property 값. 값의 Java 타입은 {@link String}이다.
	 */
	public static final class StringPropertyValue extends PropertyValue<String> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:string";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	문자열 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:string} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:string} 값으로 해석할 수 없는 경우.
		 */
		public static StringPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new StringPropertyValue(DataTypes.STRING.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:int} 타입의 {@link IntegerPropertyValue}를 생성한다.
	 *
	 * @param value	정수 값({@code null} 허용).
	 * @return	생성된 {@link IntegerPropertyValue}.
	 */
	public static IntegerPropertyValue INTEGER(Integer value) {
		return new IntegerPropertyValue(value);
	}
	/**
	 * XSD {@code xs:int} 타입 Property 값. 값의 Java 타입은 {@link Integer}이다.
	 */
	public static final class IntegerPropertyValue extends PropertyValue<Integer> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:integer";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	정수 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:int} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:int} 값으로 해석할 수 없는 경우.
		 */
		public static IntegerPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new IntegerPropertyValue(DataTypes.INT.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:float} 타입의 {@link FloatPropertyValue}를 생성한다.
	 *
	 * @param value	float 값({@code null} 허용).
	 * @return	생성된 {@link FloatPropertyValue}.
	 */
	public static FloatPropertyValue FLOAT(Float value) {
		return new FloatPropertyValue(value);
	}
	/**
	 * XSD {@code xs:float} 타입 Property 값. 값의 Java 타입은 {@link Float}이다.
	 */
	public static final class FloatPropertyValue extends PropertyValue<Float> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:float";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	float 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:float} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:float} 값으로 해석할 수 없는 경우.
		 */
		public static FloatPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new FloatPropertyValue(DataTypes.FLOAT.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:double} 타입의 {@link DoublePropertyValue}를 생성한다.
	 *
	 * @param value	double 값({@code null} 허용).
	 * @return	생성된 {@link DoublePropertyValue}.
	 */
	public static DoublePropertyValue DOUBLE(Double value) {
		return new DoublePropertyValue(value);
	}
	/**
	 * XSD {@code xs:double} 타입 Property 값. 값의 Java 타입은 {@link Double}이다.
	 */
	public static final class DoublePropertyValue extends PropertyValue<Double> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:double";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	double 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:double} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:double} 값으로 해석할 수 없는 경우.
		 */
		public static DoublePropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new DoublePropertyValue(DataTypes.DOUBLE.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:boolean} 타입의 {@link BooleanPropertyValue}를 생성한다.
	 *
	 * @param value	boolean 값({@code null} 허용).
	 * @return	생성된 {@link BooleanPropertyValue}.
	 */
	public static BooleanPropertyValue BOOLEAN(Boolean value) {
		return new BooleanPropertyValue(value);
	}
	/**
	 * XSD {@code xs:boolean} 타입 Property 값. 값의 Java 타입은 {@link Boolean}이다.
	 */
	public static final class BooleanPropertyValue extends PropertyValue<Boolean> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:boolean";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	boolean 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:boolean} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:boolean} 값으로 해석할 수 없는 경우.
		 */
		public static BooleanPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new BooleanPropertyValue(DataTypes.BOOLEAN.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:dateTime} 타입의 {@link DateTimePropertyValue}를 생성한다.
	 *
	 * @param value	시각 값({@code null} 허용).
	 * @return	생성된 {@link DateTimePropertyValue}.
	 */
	public static DateTimePropertyValue DATE_TIME(Instant value) {
		return new DateTimePropertyValue(value);
	}
	/**
	 * XSD {@code xs:dateTime} 타입 Property 값. 값의 Java 타입은 {@link Instant}이다.
	 */
	public static final class DateTimePropertyValue extends PropertyValue<Instant> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:dateTime";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	시각 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:dateTime} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:dateTime} 값으로 해석할 수 없는 경우.
		 */
		public static DateTimePropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new DateTimePropertyValue(DataTypes.DATE_TIME.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:duration} 타입의 {@link DurationPropertyValue}를 생성한다.
	 *
	 * @param value	기간 값({@code null} 허용).
	 * @return	생성된 {@link DurationPropertyValue}.
	 */
	public static DurationPropertyValue DURATION(Duration value) {
		return new DurationPropertyValue(value);
	}
	/**
	 * XSD {@code xs:duration} 타입 Property 값.
	 * <p>
	 * 다른 타입과 달리 값을 ISO-8601 duration 문자열로 직렬화한다
	 * ({@link #toValueJsonString()}/{@link #toValueJsonNode()}/{@link #serializeValue(JsonGenerator)} 재정의).
	 */
	public static final class DurationPropertyValue extends PropertyValue<Duration> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:duration";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	기간 값({@code null} 허용).
		 */
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
		public JsonNode toValueJsonNode() {
			return (m_value != null) ? TextNode.valueOf(DataTypes.DURATION.toValueString(m_value))
										: NullNode.getInstance();
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}

		@Override
		public void serializeValue(JsonGenerator gen) throws IOException {
			// value-only JSON(toValueJsonString/toValueJsonNode)과 같은 문자열 표현을 쓴다.
			String str = (m_value != null) ? DataTypes.DURATION.toValueString(m_value) : null;
			gen.writeObject(str);
		}
		
		/**
		 * JSON 노드를 {@code xs:duration} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:duration} 값으로 해석할 수 없는 경우.
		 */
		public static DurationPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new DurationPropertyValue(DataTypes.DURATION.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:long} 타입의 {@link LongPropertyValue}를 생성한다.
	 *
	 * @param value	long 정수 값({@code null} 허용).
	 * @return	생성된 {@link LongPropertyValue}.
	 */
	public static LongPropertyValue LONG(Long value) {
		return new LongPropertyValue(value);
	}
	/**
	 * XSD {@code xs:long} 타입 Property 값. 값의 Java 타입은 {@link Long}이다.
	 */
	public static final class LongPropertyValue extends PropertyValue<Long> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:long";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	long 정수 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:long} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:long} 값으로 해석할 수 없는 경우.
		 */
		public static LongPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new LongPropertyValue(DataTypes.LONG.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:short} 타입의 {@link ShortPropertyValue}를 생성한다.
	 *
	 * @param value	short 정수 값({@code null} 허용).
	 * @return	생성된 {@link ShortPropertyValue}.
	 */
	public static ShortPropertyValue SHORT(Short value) {
		return new ShortPropertyValue(value);
	}
	/**
	 * XSD {@code xs:short} 타입 Property 값. 값의 Java 타입은 {@link Short}이다.
	 */
	public static final class ShortPropertyValue extends PropertyValue<Short> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:short";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	short 정수 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:short} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:short} 값으로 해석할 수 없는 경우.
		 */
		public static ShortPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new ShortPropertyValue(DataTypes.SHORT.fromJsonNode(vnode));
		}
	}

	/**
	 * 주어진 값을 갖는 {@code xs:decimal} 타입의 {@link DecimalPropertyValue}를 생성한다.
	 *
	 * @param value	십진수 값({@code null} 허용).
	 * @return	생성된 {@link DecimalPropertyValue}.
	 */
	public static DecimalPropertyValue DECIMAL(BigDecimal value) {
		return new DecimalPropertyValue(value);
	}
	/**
	 * XSD {@code xs:decimal} 타입 Property 값. 값의 Java 타입은 {@link BigDecimal}이다.
	 */
	public static final class DecimalPropertyValue extends PropertyValue<BigDecimal> {
		/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
		public static final String SERIALIZATION_TYPE = "mdt:value:decimal";
		
		/**
		 * 주어진 값으로 객체를 생성한다.
		 *
		 * @param value	십진수 값({@code null} 허용).
		 */
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
		
		/**
		 * JSON 노드를 {@code xs:decimal} 값으로 해석하여 객체를 생성한다.
		 * <p>
		 * polymorphic JSON의 {@code value} 하위 노드나 value-only JSON 노드에 사용된다. JSON {@code null}은
		 * {@code null} 값으로 해석된다.
		 *
		 * @param vnode	값에 해당하는 JSON 노드.
		 * @return	생성된 객체.
		 * @throws IOException	JSON 노드를 {@code xs:decimal} 값으로 해석할 수 없는 경우.
		 */
		public static DecimalPropertyValue deserializeValue(JsonNode vnode) throws IOException {
			return new DecimalPropertyValue(DataTypes.DECIMAL.fromJsonNode(vnode));
		}
	}
}
