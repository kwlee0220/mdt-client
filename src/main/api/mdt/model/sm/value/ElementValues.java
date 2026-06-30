package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.sm.value.PropertyValue.BooleanPropertyValue;
import mdt.model.sm.value.PropertyValue.DateTimePropertyValue;
import mdt.model.sm.value.PropertyValue.DecimalPropertyValue;
import mdt.model.sm.value.PropertyValue.DoublePropertyValue;
import mdt.model.sm.value.PropertyValue.DurationPropertyValue;
import mdt.model.sm.value.PropertyValue.FloatPropertyValue;
import mdt.model.sm.value.PropertyValue.IntegerPropertyValue;
import mdt.model.sm.value.PropertyValue.LongPropertyValue;
import mdt.model.sm.value.PropertyValue.ShortPropertyValue;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;


/**
 * {@link SubmodelElement}와 {@link ElementValue} 사이의 변환, 그리고 {@link ElementValue}의 JSON
 * 직렬화/역직렬화를 담당하는 정적 유틸리티.
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>{@link SubmodelElement}에서 값 부분을 추출({@link #getValue(SubmodelElement)})하거나,
 *       값 객체/JSON으로부터 {@link ElementValue}를 생성({@link #fromValueObject(Object, SubmodelElement)},
 *       {@link #parseValueJsonNode(JsonNode, SubmodelElement)},
 *       {@link #parseValueJsonString(String, SubmodelElement)}).</li>
 *   <li>{@link ElementValue}를 {@link SubmodelElement}에 반영({@link #update(SubmodelElement, ElementValue)}).</li>
 *   <li>{@code @type}/{@code value} 형태의 polymorphic JSON 직렬화({@link #serializeJson})·
 *       역직렬화({@link #parseJsonNode(JsonNode)}/{@link #parseJsonString(String)}). Jackson 연동용
 *       {@link Serializer}/{@link Deserializer}가 이를 사용한다.</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementValues {
	private static final String FIELD_TYPE = "@type";
	private static final String FIELD_VALUE = "value";

	// ******************************************************
	// 	SubmodelElement -> ElementValue
	// ******************************************************
	/**
	 * 주어진 {@link SubmodelElement}에서 대한 value에 해당하는 부분만을 추출하여
	 * {@link ElementValue} 형태로 반환한다.
	 *
	 * @param element 값을 추출할 {@link SubmodelElement} 객체.
	 * @return 추출된 {@link ElementValue} 객체. 인자 element가 {@code null}이면 {@code null} 반환.
	 */
	public static ElementValue getValue(@Nullable SubmodelElement element) {
		if ( element == null ) {
			return null;
		}
		
		return switch ( element ) {
			case Property prop -> PropertyValue.from(prop);
			case SubmodelElementCollection smc -> ElementCollectionValue.from(smc);
			case SubmodelElementList sml -> ElementListValue.from(sml);
			case File aasFile -> FileValue.from(aasFile);
			case MultiLanguageProperty mlp -> MultiLanguagePropertyValue.from(mlp);
			case Range rg -> RangeValue.from(rg);
			case ReferenceElement ref -> ReferenceElementValue.from(ref);
			default -> throw new IllegalArgumentException(String.format("Unsupported SubmodelElement(%s) for 'getValue(SubmodelElement)'",
																	element.getClass()));
		};
	}
	
	// ******************************************************
	// 	JsonNode, Json -> ElementValue
	// ******************************************************
	/**
	 * {@code @type}/{@code value} 형태의 polymorphic JSON 노드를 {@link ElementValue}로 역직렬화한다.
	 * <p>
	 * {@code @type} 필드의 직렬화 식별자로 구체 타입을 판별하여 해당 타입의 {@code deserializeValue}를 호출한다.
	 *
	 * @param jnode	{@code @type}과 {@code value} 필드를 가진 JSON 노드.
	 * @return 역직렬화된 ElementValue.
	 * @throws IOException {@code @type} 필드가 없는 경우.
	 * @throws JacksonDeserializationException	등록되지 않은 타입인 경우.
	 */
	public static ElementValue parseJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new IOException(String.format("'%s' field is missing: json=%s", FIELD_TYPE, jnode));
		}
		
		JsonNode valueNode = JacksonUtils.getFieldOrNull(jnode, FIELD_VALUE);
		switch ( type ) {
			case StringPropertyValue.SERIALIZATION_TYPE:
				return StringPropertyValue.deserializeValue(valueNode);
			case IntegerPropertyValue.SERIALIZATION_TYPE:
				return IntegerPropertyValue.deserializeValue(valueNode);
			case ElementListValue.SERIALIZATION_TYPE:
				return ElementListValue.deserializeValue(valueNode);
			case ElementCollectionValue.SERIALIZATION_TYPE:
				return ElementCollectionValue.deserializeValue(valueNode);
			case DoublePropertyValue.SERIALIZATION_TYPE:
				return DoublePropertyValue.deserializeValue(valueNode);
			case FloatPropertyValue.SERIALIZATION_TYPE:
				return FloatPropertyValue.deserializeValue(valueNode);
			case BooleanPropertyValue.SERIALIZATION_TYPE:
				return BooleanPropertyValue.deserializeValue(valueNode);
			case DateTimePropertyValue.SERIALIZATION_TYPE:
				return DateTimePropertyValue.deserializeValue(valueNode);
			case DurationPropertyValue.SERIALIZATION_TYPE:
				return DurationPropertyValue.deserializeValue(valueNode);
			case FileValue.SERIALIZATION_TYPE:
				return FileValue.deserializeValue(valueNode);
			case LongPropertyValue.SERIALIZATION_TYPE:
				return LongPropertyValue.deserializeValue(valueNode);
			case ShortPropertyValue.SERIALIZATION_TYPE:
				return ShortPropertyValue.deserializeValue(valueNode);
			case RangeValue.SERIALIZATION_TYPE:
				return RangeValue.deserializeValue(valueNode);
			case MultiLanguagePropertyValue.SERIALIZATION_TYPE:
				return MultiLanguagePropertyValue.deserializeValue(valueNode);
			case OperationVariableValue.SERIALIZATION_TYPE:
				return OperationVariableValue.deserializeValue(valueNode);
			case ReferenceElementValue.SERIALIZATION_TYPE:
				return ReferenceElementValue.deserializeValue(valueNode);
			case DecimalPropertyValue.SERIALIZATION_TYPE:
				return DecimalPropertyValue.deserializeValue(valueNode);
			default:
				throw new JacksonDeserializationException("Unregistered ElementValue type: " + type);
		}
	}
	
	/**
	 * {@code @type}/{@code value} 형태의 polymorphic JSON 문자열을 {@link ElementValue}로 역직렬화한다.
	 *
	 * @param json	JSON 문자열.
	 * @return 역직렬화된 ElementValue.
	 * @throws IOException	JSON 파싱이 실패한 경우.
	 */
	public static ElementValue parseJsonString(String json) throws IOException {
		JsonNode jnode = MDTModelSerDe.getJsonMapper().readTree(json);
		return parseJsonNode(jnode);
	}
	
	
	// ******************************************************
	// 	value object (Java) -> ElementValue
	// ******************************************************
	/**
	 * {@link ElementValue} proto를 참조하여 주어진 값 객체(vo)를 파싱하여 {@link ElementValue}
	 * 객체로 반환한다.
	 *
	 * @param vo    파싱할 값 객체.
	 * @param proto 참조할 {@link ElementValue} 객체.
	 * @return 파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 값 객체가 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue fromValueObject(Object vo, ElementValue proto) throws IOException {
		return switch ( proto ) {
			case PropertyValue<?> prop -> PropertyValue.fromValueObject(vo, prop.getDataType().getTypeDefXsd());
			case ElementCollectionValue smcv -> ElementCollectionValue.fromValueObject(vo, smcv);
			case ElementListValue sml -> ElementListValue.fromValueObject(vo, sml);
			case FileValue aasFile -> FileValue.fromValueObject(vo);
			case MultiLanguagePropertyValue mlp -> MultiLanguagePropertyValue.fromValueObject(vo);
			case RangeValue<?> rg -> RangeValue.fromValueObject(vo, rg.getValueType().getTypeDefXsd());
			case ReferenceElementValue ref -> ReferenceElementValue.fromValueObject(vo, ref.getReference().getType());
			default -> throw new IllegalArgumentException(String.format("Unsupported ElementValue(%s) for 'fromValueObject(Object, SubmodelElement)'",
																	proto.getClass()));
		};
	}
	
	
	// ******************************************************
	// 	value JsonNode/JSON -> ElementValue
	// ******************************************************
	/**
	 * {@link ElementValue} proto를 참조하여 주어진 JSON 노드(vnode)를 파싱하여
	 * {@link ElementValue} 객체로 반환한다.
	 *
	 * @param vnode	파싱할 JSON 노드.
	 * @param proto	참조할 {@link ElementValue} 객체.
	 * @return	파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 JSON 노드가 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue parseValueJsonNode(JsonNode vnode, ElementValue proto) throws IOException {
		return switch ( proto ) {
			case PropertyValue<?> prop -> PropertyValue.parseValueJsonNode(vnode, prop);
			case ElementCollectionValue smc -> ElementCollectionValue.parseValueJsonNode(vnode, smc);
			case ElementListValue sml -> ElementListValue.parseValueJsonNode(vnode, sml);
			case FileValue aasFile -> FileValue.parseValueJsonNode(vnode);
			case MultiLanguagePropertyValue mlp -> MultiLanguagePropertyValue.parseValueJsonNode(vnode);
			case RangeValue<?> rg -> RangeValue.parseValueJsonNode(vnode, rg.getValueType().getTypeDefXsd());
			case ReferenceElementValue ref -> ReferenceElementValue.parseValueJsonNode(vnode, ref.getReference().getType());
			default -> throw new IllegalArgumentException(String.format("Unsupported SubmodelElement(%s) for 'parseValueJsonNode(JsonNode, SubmodelElement)'",
																	proto.getClass()));
		};
	}
	
	/**
	 * {@link ElementValue} proto를 참조하여 주어진 JSON 문자열(valueJsonString)을 파싱하여
	 * {@link ElementValue} 객체로 반환한다.
	 *
	 * @param valueJsonString 파싱할 JSON 문자열.
	 * @param proto           참조할 {@link ElementValue} 객체.
	 * @return 파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 JSON 문자열이 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue parseValueJsonString(String valueJsonString, ElementValue proto)
		throws IOException {
		JsonNode jnode = MDTModelSerDe.getJsonMapper().readTree(valueJsonString);
		return parseValueJsonNode(jnode, proto);
	}
	
	
	// ******************************************************
	// 	value literal (string format) -> ElementValue
	// ******************************************************
	/**
	 * MDT 값 표현식 문자열을 평가하여 {@link ElementValue}를 반환한다.
	 *
	 * @param expr	값 리터럴 표현식.
	 * @return 평가 결과 ElementValue.
	 */
	public static ElementValue parseExpr(String expr) {
		return MDTExpressionParser.parseValueLiteral(expr).evaluate();
	}
	
	
	// ******************************************************
	// 	update SubmodelElement with ElementValue
	// ******************************************************
	/**
	 * 주어진 {@link SubmodelElement} sme 객체에 대한 값을 smev로 갱신한다.
	 * 
	 * @param sme	갱신할 {@link SubmodelElement} 객체.
	 * @param smev	갱신할 값.
	 */
	public static SubmodelElement update(SubmodelElement sme, ElementValue smev) {
		if ( sme instanceof Property prop ) {
			Preconditions.checkArgument(smev instanceof PropertyValue,
										"Expecting %s, but %s", PropertyValue.class.getName(),
										smev.getClass().getName());
			((PropertyValue<?>)smev).update(prop);
		}
		else if ( sme instanceof SubmodelElementCollection smc ) {
			if ( smev instanceof ElementCollectionValue smcv ) {
				smcv.update(smc);
			}
			else {
				String msg = String.format("Expecting %s, but %s",
										ElementCollectionValue.class.getName(), smev.getClass().getName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			if ( smev instanceof ElementListValue smlv ) {
				smlv.update(sml);
			}
			else {
				String msg = String.format("Expecting %s, but %s",
											ElementListValue.class.getName(), smev.getClass().getName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( sme instanceof File aasFile ) {
			Preconditions.checkArgument(smev instanceof FileValue,
										"Expecting %s, but %s", FileValue.class.getName(), smev.getClass().getName());
			((FileValue)smev).update(aasFile);
		}
		else if ( sme instanceof Range rg ) {
			Preconditions.checkArgument(smev instanceof RangeValue,
										"Expecting %s, but %s", RangeValue.class.getName(), smev.getClass().getName());
			((RangeValue<?>)smev).update(rg);
		}
		else if ( sme instanceof MultiLanguageProperty mlprop ) {
			Preconditions.checkArgument(smev instanceof MultiLanguagePropertyValue,
										"Expecting %s, but %s", MultiLanguagePropertyValue.class.getName(),
																smev.getClass().getName());
			((MultiLanguagePropertyValue)smev).update(mlprop);
		}
		else if ( sme instanceof ReferenceElement refElm ) {
			Preconditions.checkArgument(smev instanceof ReferenceElementValue,
										"Expecting %s, but %s", ReferenceElementValue.class.getName(),
																smev.getClass().getName());
			((ReferenceElementValue)smev).update(refElm);
		}
		else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'update(SubmodelElement, ElementValue)'",
										sme.getClass());
			throw new IllegalArgumentException(msg);
		}
		
		return sme;
	}
	
	/**
	 * valueNode를 이용하여 주어진 {@link SubmodelElement} sme 객체에 대한 값을 갱신한다.
	 * 
	 * @param sme       갱신할 {@link SubmodelElement} 객체.
	 * @param valueNode 갱신할 값이 포함된 JSON 노드.
	 */
	public static SubmodelElement update(SubmodelElement sme, JsonNode valueNode) throws IOException {
		ElementValue proto = getValue(sme);
		ElementValue value = parseValueJsonNode(valueNode, proto);
		update(sme, value);
		
		return sme;
	}
	
	/**
	 * 주어진 JSON 문자열의 값으로 대상 {@link SubmodelElement}를 갱신한다.
	 *
	 * @param target			갱신할 SubmodelElement.
	 * @param valueJsonString	값에 해당하는 JSON 문자열.
	 * @throws IOException	JSON 파싱 또는 갱신이 실패한 경우.
	 */
	public static void updateWithValueJsonString(SubmodelElement target, String valueJsonString)
		throws IOException {
		ElementValue proto = getValue(target);
		ElementValue smev = parseValueJsonString(valueJsonString, proto);
		update(target, smev);
	}
	
	/**
	 * {@link AbstractElementValue}를 {@code @type}/{@code value} 형태의 polymorphic JSON으로 직렬화하는
	 * Jackson serializer. {@link ElementValues#serializeJson(AbstractElementValue, JsonGenerator)}에 위임한다.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<AbstractElementValue> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<AbstractElementValue> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(AbstractElementValue value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			serializeJson(value, gen);
		}
	}

	/**
	 * {@code @type}/{@code value} 형태의 JSON을 {@link ElementValue}로 역직렬화하는 Jackson
	 * deserializer. {@link ElementValues#parseJsonNode(JsonNode)}에 위임한다.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<ElementValue> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public ElementValue deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseJsonNode(jnode);
		}
	}

	/**
	 * 주어진 {@link AbstractElementValue}를 {@code @type}/{@code value} 형태의 JSON으로 직렬화한다.
	 *
	 * @param value	직렬화할 값.
	 * @param gen	직렬화에 사용할 JsonGenerator.
	 * @throws IOException	직렬화가 실패한 경우.
	 */
	public static void serializeJson(AbstractElementValue value, JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_TYPE, value.getSerializationType());
		gen.writeFieldName(FIELD_VALUE);
		value.serializeValue(gen);
		gen.writeEndObject();
	}
	
	/**
	 * 문자열 양끝의 큰따옴표를 제거한다.
	 * <p>
	 * 앞뒤 공백을 제거한 뒤 큰따옴표로 둘러싸인 경우에만 내부 문자열을 반환하고, 그렇지 않으면 그대로 반환한다.
	 *
	 * @param str	대상 문자열({@code null} 허용).
	 * @return 따옴표를 제거한 문자열. {@code null}이면 {@code null}.
	 */
	public static String unquote(String str) {
		if (str == null) return null;
		str = str.trim();
		if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length() - 1);
		}
		return str;
	}
}
