package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.Preconditions;
import utils.func.CheckedSupplierX;
import utils.json.JacksonDeserializationException;
import utils.json.JsonNodeDeserializer;
import utils.json.JsonNodeStdDeserializer;
import utils.json.MultiTypeJsonNodeDeserializer;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.expr.MDTParserException;
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
 *       값 객체/JSON으로부터 {@link ElementValue}를 생성({@link #fromValueObject(Object, ElementValue)},
 *       {@link #parseValueJsonNode(JsonNode, ElementValue)},
 *       {@link #parseValueJsonString(String, ElementValue)}).</li>
 *   <li>{@link ElementValue}를 {@link SubmodelElement}에 반영
 *       ({@link #update(SubmodelElement, ElementValue)}).</li>
 *   <li>{@code @type}/{@code value} 형태의 polymorphic JSON 직렬화({@link #serializeJson})·
 *       역직렬화({@link #parseJsonNode(JsonNode)}/{@link #parseJsonString(String)}). Jackson 연동용
 *       {@link Serializer}/{@link Deserializer}가 이를 사용한다.</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ElementValues {
	private ElementValues() {
		throw new AssertionError("Should not be invoked!!: class=" + ElementValues.class.getName());
	}

	private static final String FIELD_VALUE = "value";

	// ElementValue 타입별 deserializer 등록부. 각 타입의 deserializeValue는 노드 전체가 아니라 'value' 하위 노드를
	// 받으므로 onValue()로 감싸 등록한다.
	// 새로운 ElementValue 타입을 추가하는 경우 반드시 여기에 등록해야
	// polymorphic JSON 역직렬화({@code parseJsonNode})가 가능하다.
	// 같은 타입 이름을 두 번 등록하면 클래스 초기화 시 IllegalArgumentException이 발생한다.
	private static final MultiTypeJsonNodeDeserializer<ElementValue> DESERIALIZER
		= MultiTypeJsonNodeDeserializer.builder(ElementValue.class)
			.register(StringPropertyValue.SERIALIZATION_TYPE, onValue(StringPropertyValue::deserializeValue))
			.register(IntegerPropertyValue.SERIALIZATION_TYPE, onValue(IntegerPropertyValue::deserializeValue))
			.register(FloatPropertyValue.SERIALIZATION_TYPE, onValue(FloatPropertyValue::deserializeValue))
			.register(DoublePropertyValue.SERIALIZATION_TYPE, onValue(DoublePropertyValue::deserializeValue))
			.register(BooleanPropertyValue.SERIALIZATION_TYPE, onValue(BooleanPropertyValue::deserializeValue))
			.register(DateTimePropertyValue.SERIALIZATION_TYPE, onValue(DateTimePropertyValue::deserializeValue))
			.register(DurationPropertyValue.SERIALIZATION_TYPE, onValue(DurationPropertyValue::deserializeValue))
			.register(LongPropertyValue.SERIALIZATION_TYPE, onValue(LongPropertyValue::deserializeValue))
			.register(ShortPropertyValue.SERIALIZATION_TYPE, onValue(ShortPropertyValue::deserializeValue))
			.register(DecimalPropertyValue.SERIALIZATION_TYPE, onValue(DecimalPropertyValue::deserializeValue))
			.register(ElementCollectionValue.SERIALIZATION_TYPE, onValue(ElementCollectionValue::deserializeValue))
			.register(ElementListValue.SERIALIZATION_TYPE, onValue(ElementListValue::deserializeValue))
			.register(FileValue.SERIALIZATION_TYPE, onValue(FileValue::deserializeValue))
			.register(MultiLanguagePropertyValue.SERIALIZATION_TYPE, onValue(MultiLanguagePropertyValue::deserializeValue))
			.register(RangeValue.SERIALIZATION_TYPE, onValue(RangeValue::deserializeValue))
			.register(ReferenceElementValue.SERIALIZATION_TYPE, onValue(ReferenceElementValue::deserializeValue))
			.register(OperationVariableValue.SERIALIZATION_TYPE, onValue(OperationVariableValue::deserializeValue))
			.build();
	// 타입 필드 이름. 직렬화 쪽과 항상 같도록 등록부에서 가져온다.
	private static final String FIELD_TYPE = DESERIALIZER.getTypeField();

	/**
	 * {@code value} 하위 노드를 받는 deserializer를, {@code @type}/{@code value} 노드 전체를 받는
	 * deserializer로 바꾼다. {@code value}가 JSON {@code null}이면 그대로 넘긴다.
	 */
	private static JsonNodeDeserializer<ElementValue>
	onValue(JsonNodeDeserializer<? extends ElementValue> valueDeserializer) {
		return jnode -> {
			JsonNode vnode = jnode.get(FIELD_VALUE);
			if ( vnode == null ) {
				throw new JacksonDeserializationException(String.format("'%s' field is missing: type=%s",
																		FIELD_VALUE, jnode.get(FIELD_TYPE).asText()));
			}
			return valueDeserializer.deserialize(vnode);
		};
	}

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
			case Operation op -> OperationVariableValue.from(op);
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
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이나 {@code value} 필드가 없거나, {@code @type}이
	 * 										문자열이 아니거나, 미등록 타입인 경우.
	 * @throws IOException	구현체가 {@code value}를 해석하지 못한 경우.
	 */
	public static ElementValue parseJsonNode(JsonNode jnode) throws IOException {
		return DESERIALIZER.deserialize(jnode);
	}
	
	/**
	 * {@code @type}/{@code value} 형태의 polymorphic JSON 문자열을 {@link ElementValue}로 역직렬화한다.
	 * <p>
	 * 문자열 전체가 JSON 값 하나여야 한다(첫 값 뒤의 입력은 거부, 뒤의 공백은 허용). 트리로 읽은 뒤
	 * {@link #parseJsonNode(JsonNode)}에 넘기므로 JSON {@code null}({@code "null"})은 {@code @type} 누락으로 거부한다.
	 *
	 * @param json	JSON 문자열.
	 * @return 역직렬화된 ElementValue.
	 * @throws IOException	입력이 비었거나, JSON 문법이 틀렸거나, 첫 값 뒤에 다른 입력이 있거나, 값을 해석할 수 없는 경우.
	 */
	public static ElementValue parseJsonString(String json) throws IOException {
		// 첫 JSON 값 뒤의 입력(예: '{...} garbage')은 거부한다.
		JsonNode jnode = MDTModelSerDe.readSingleJsonNode(json);
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
			case OperationVariableValue opv -> OperationVariableValue.fromValueObject(vo, opv);
			// ElementValue는 봉인되어 있으므로 default가 없다. 값 타입이 늘면 여기서 컴파일 오류가 난다.
		};
	}
	
	/**
	 * {@code @type} 필드의 존재 여부에 따라 주어진 JSON 노드를 {@link ElementValue}로 파싱한다.
	 * <p>
	 * {@code @type} 필드가 있는 경우는 그 타입 식별자로 값의 타입이 결정되고({@link #parseJsonNode(JsonNode)}),
	 * 없는 경우(필드가 없거나 JSON {@code null}, 또는 노드가 객체가 아닌 경우)는 {@code proto}의 타입을 따른다
	 * ({@link #parseValueJsonNode(JsonNode, ElementValue)}).
	 * <p>
	 * {@code @type}이 있으면 {@code proto}는 쓰지 않으며, {@code @type}이 {@code proto}의 타입과 같은지도 확인하지
	 * 않는다. 예를 들어 {@code proto}가 {@code xs:int} {@link PropertyValue}여도 {@code @type}이
	 * {@code mdt:value:string}이면 문자열 값이 반환된다. 대상 원소의 타입과 맞는지는 반영할 때
	 * ({@link #update(SubmodelElement, ElementValue)}) 확인된다.
	 *
	 * @param jnode	파싱할 JSON 노드.
	 * @param proto	{@code @type} 필드가 없는 경우 참조할 {@link ElementValue} 객체.
	 * @return	파싱된 {@link ElementValue} 객체.
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 JSON 문자열이 아니거나, 빈 문자열이거나, 미등록 타입인 경우.
	 * @throws IOException				JSON 해석 과정에서 오류가 발생한 경우.
	 */
	public static ElementValue parseJsonNode(JsonNode jnode, ElementValue proto) throws IOException {
		return parseJsonNode(jnode, () -> proto);
	}

	/**
	 * {@code @type} 필드의 존재 여부에 따라 주어진 JSON 노드를 {@link ElementValue}로 파싱한다.
	 * <p>
	 * {@link #parseJsonNode(JsonNode, ElementValue)}와 동일하지만, {@code proto}를 얻는 작업이
	 * 원격 접근 등으로 비용이 큰 경우를 위해 실제로 필요한 경우(= {@code @type} 필드가 없는 경우)에만
	 * {@code protoSupplier}를 호출한다. {@code @type}과 {@code proto}의 타입이 같은지 확인하지 않는 것도 같다.
	 *
	 * @param jnode			파싱할 JSON 노드.
	 * @param protoSupplier	{@code @type} 필드가 없는 경우 참조할 {@link ElementValue} 공급자.
	 * @return	파싱된 {@link ElementValue} 객체.
	 * @throws IllegalArgumentException	{@code jnode}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 JSON 문자열이 아니거나, 빈 문자열이거나, 미등록 타입인 경우.
	 * @throws IOException				JSON 해석 또는 {@code proto} 획득 과정에서 오류가 발생한 경우.
	 */
	public static ElementValue parseJsonNode(JsonNode jnode,
													CheckedSupplierX<ElementValue,IOException> protoSupplier)
		throws IOException {
		Preconditions.checkNotNullArgument(jnode, "JsonNode is null");

		JsonNode typeNode = jnode.isObject() ? jnode.get(FIELD_TYPE) : null;
		if ( typeNode == null || typeNode.isNull() ) {
			// '@type' 필드가 없는 경우는 proto의 타입을 그대로 사용한다.
			return parseValueJsonNode(jnode, protoSupplier.get());
		}
		if ( !typeNode.isTextual() ) {
			throw new JacksonDeserializationException(String.format("invalid field '%s': string expected, but %s",
																	FIELD_TYPE,
																	typeNode.getNodeType().name().toLowerCase()));
		}
		if ( typeNode.textValue().isBlank() ) {
			throw new JacksonDeserializationException(String.format("invalid field '%s': blank", FIELD_TYPE));
		}
		// 모르는 타입은 등록부가 JacksonDeserializationException으로 거부한다.
		return parseJsonNode(jnode);
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
			case OperationVariableValue opv -> OperationVariableValue.parseValueJsonNode(vnode, opv);
			// ElementValue는 봉인되어 있으므로 default가 없다. 값 타입이 늘면 여기서 컴파일 오류가 난다.
		};
	}
	
	/**
	 * {@link ElementValue} proto를 참조하여 주어진 JSON 문자열(valueJsonString)을 파싱하여
	 * {@link ElementValue} 객체로 반환한다.
	 * <p>
	 * 문자열 전체가 JSON 값 하나여야 한다. 첫 값 뒤에 다른 입력이 있으면(예: {@code "3.5 sec"}, {@code "5 garbage"})
	 * 거부한다. 뒤의 공백·개행은 허용한다. {@link #updateFromValueJson(SubmodelElement, String)}도 이 메소드를 거친다.
	 *
	 * @param valueJsonString 파싱할 JSON 문자열.
	 * @param proto           참조할 {@link ElementValue} 객체.
	 * @return 파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력이 비었거나, JSON 문법이 틀렸거나, 첫 값 뒤에 다른 입력이 있거나, 참조 proto와 맞지 않는 경우.
	 */
	public static ElementValue parseValueJsonString(String valueJsonString, ElementValue proto)
		throws IOException {
		JsonNode jnode = MDTModelSerDe.readSingleJsonNode(valueJsonString);
		return parseValueJsonNode(jnode, proto);
	}
	
	
	// ******************************************************
	// 	value literal (string format) -> ElementValue
	// ******************************************************
	/**
	 * MDT 값 표현식 문자열을 평가하여 {@link ElementValue}를 반환한다.
	 *
	 * @param expr	값 리터럴 표현식(예: {@code 17}, {@code 'abc'}, {@code true}).
	 * @return 평가 결과 ElementValue.
	 * @throws IllegalArgumentException	{@code expr}이 {@code null}인 경우.
	 * @throws MDTParserException	표현식의 문법이 틀렸거나 뒤에 남는 입력이 있는 경우.
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
	 * @return	{@code sme}.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나, {@code sme}와 {@code smev}의 종류·데이터 타입이
	 * 									맞지 않는 경우.
	 */
	public static SubmodelElement update(SubmodelElement sme, ElementValue smev) {
		Preconditions.checkNotNullArgument(sme, "SubmodelElement is null");
		Preconditions.checkNotNullArgument(smev, "ElementValue is null");

		switch ( sme ) {
			case Property prop -> expect(PropertyValue.class, smev).update(prop);
			case SubmodelElementCollection smc -> expect(ElementCollectionValue.class, smev).update(smc);
			case SubmodelElementList sml -> expect(ElementListValue.class, smev).update(sml);
			case File aasFile -> expect(FileValue.class, smev).update(aasFile);
			case Range rg -> expect(RangeValue.class, smev).update(rg);
			case MultiLanguageProperty mlprop -> expect(MultiLanguagePropertyValue.class, smev).update(mlprop);
			case ReferenceElement refElm -> expect(ReferenceElementValue.class, smev).update(refElm);
			case Operation op -> expect(OperationVariableValue.class, smev).update(op);
			// SubmodelElement(aas4j)는 봉인되어 있지 않으므로 default가 필요하다.
			default -> throw new IllegalArgumentException(String.format("Unsupported SubmodelElement(%s) for 'update(SubmodelElement, ElementValue)'",
																		sme.getClass().getName()));
		}
		
		return sme;
	}
	
	/** {@code smev}가 {@code cls} 타입이 아니면 거부한다. 메시지는 이전 형식("Expecting X, but Y")을 유지한다. */
	@SuppressWarnings("unchecked")
	private static <V extends ElementValue> V expect(Class<V> cls, ElementValue smev) {
		if ( !cls.isInstance(smev) ) {
			throw new IllegalArgumentException(String.format("Expecting %s, but %s", cls.getName(),
															smev.getClass().getName()));
		}
		return (V)smev;
	}

	/**
	 * value-only JSON 노드로 {@link SubmodelElement}를 갱신한다. 값은 {@code sme}의 현재 값을 proto로 삼아 해석한다
	 * ({@link #parseValueJsonNode(JsonNode, ElementValue)}).
	 *
	 * @param sme		갱신할 SubmodelElement.
	 * @param valueNode	값 JSON 노드. JSON {@code null}이면 {@code NullNode}로 넘긴다.
	 * @return	{@code sme}.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code sme}의 데이터 타입과 맞지 않는 경우.
	 * @throws IOException	값을 해석할 수 없는 경우.
	 */
	public static SubmodelElement updateFromValueJson(SubmodelElement sme, JsonNode valueNode) throws IOException {
		// JSON null 값은 NullNode로 넘긴다. Java null은 거부한다.
		Preconditions.checkNotNullArgument(sme, "SubmodelElement is null");
		Preconditions.checkNotNullArgument(valueNode, "value JsonNode is null");
		ElementValue proto = getValue(sme);
		ElementValue value = parseValueJsonNode(valueNode, proto);
		update(sme, value);
		
		return sme;
	}
	
	/**
	 * AAS 값 문자열({@code xs:*} 표기, 예: {@code abc}, {@code 42})로 {@link SubmodelElement}를 갱신한다.
	 * JSON 문자열이 아니다 — JSON은 {@link #updateFromValueJson(SubmodelElement, String)}를 쓴다.
	 * <p>
	 * {@code sme}가 {@code valueType}이 있는 {@link Property}이면 문자열을 그 데이터 타입으로 해석할 수 있는지 확인한 뒤
	 * 그대로 기록한다(예: {@code xs:int} 속성에 {@code "42"}는 받고 {@code "abc"}는 거부). 그 밖의 경우는
	 * {@code xs:string} 값({@link PropertyValue#STRING(String)})으로 갱신한다.
	 *
	 * @param sme	갱신할 SubmodelElement.
	 * @param value	값 문자열.
	 * @return	{@code sme}.
	 * @throws IllegalArgumentException	대상 데이터 타입으로 해석할 수 없거나 Property가 아닌 경우.
	 */
	public static SubmodelElement updateFromValueString(SubmodelElement sme, @Nullable String value) {
		// value가 null이면 Property의 값을 비운다.
		Preconditions.checkNotNullArgument(sme, "SubmodelElement is null");
		if ( sme instanceof Property prop && prop.getValueType() != null ) {
			prop.setValue(PropertyValue.checkValueString(prop, value));
			return sme;
		}
		update(sme, PropertyValue.STRING(value));
		return sme;
	}
	/**
	 * Java 숫자 값으로 {@link SubmodelElement}를 갱신한다.
	 * <p>
	 * 값의 타입에 맞는 {@link PropertyValue}({@code Integer}→{@code xs:int}, {@code Long}→{@code xs:long},
	 * {@code Short}·{@code Byte}→{@code xs:short}, {@code Float}→{@code xs:float},
	 * {@code Double}→{@code xs:double}, {@code BigDecimal}·{@code BigInteger}·그 밖→{@code xs:decimal})로 감싸
	 * {@link #update(SubmodelElement, ElementValue)}에 넘긴다. 따라서 Property의 데이터 타입과 다르면 숫자끼리의
	 * 손실 없는 변환만 허용된다(예: {@code 5} → {@code xs:long}).
	 * {@code null}이면 속성 값을 비운다.
	 *
	 * @param sme	갱신할 SubmodelElement.
	 * @param value	값({@code null} 허용).
	 * @return	{@code sme}.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 Property가 아니거나, 손실 없이 변환할 수 없는 경우.
	 */
	public static SubmodelElement updateValue(SubmodelElement sme, @Nullable Number value) {
		PropertyValue<?> pv = switch ( value ) {
			// 값이 없으면 타입과 상관없이 속성 값을 비운다(PropertyValue.update는 null 값의 타입을 검사하지 않는다).
			case null -> PropertyValue.INTEGER(null);
			case Integer i -> PropertyValue.INTEGER(i);
			case Long l -> PropertyValue.LONG(l);
			case Short sh -> PropertyValue.SHORT(sh);
			case Byte b -> PropertyValue.SHORT(b.shortValue());
			case Float f -> PropertyValue.FLOAT(f);
			case Double d -> PropertyValue.DOUBLE(d);
			case java.math.BigDecimal bd -> PropertyValue.DECIMAL(bd);
			default -> PropertyValue.DECIMAL(new java.math.BigDecimal(value.toString()));
		};
		return update(sme, pv);
	}
	
	/**
	 * value-only JSON 문자열로 대상 {@link SubmodelElement}를 갱신한다(예: 문자열 속성이면 {@code "\"abc\""},
	 * 정수 속성이면 {@code "42"}). AAS 값 문자열은 {@link #updateFromValueString(SubmodelElement, String)}를 쓴다.
	 *
	 * @param target			갱신할 SubmodelElement.
	 * @param valueJsonString	값에 해당하는 JSON 문자열.
	 * @return	{@code target}.
	 * @throws IOException	JSON 파싱 또는 갱신이 실패한 경우.
	 */
	public static SubmodelElement updateFromValueJson(SubmodelElement target, String valueJsonString)
		throws IOException {
		Preconditions.checkNotNullArgument(target, "SubmodelElement is null");
		Preconditions.checkNotNullArgument(valueJsonString, "value JSON string is null");
		ElementValue proto = getValue(target);
		ElementValue smev = parseValueJsonString(valueJsonString, proto);
		update(target, smev);
		return target;
	}
	
	/**
	 * {@link AbstractElementValue}를 {@code @type}/{@code value} 형태의 polymorphic JSON으로 직렬화하는
	 * Jackson serializer. {@link ElementValues#serializeJson(AbstractElementValue, JsonGenerator)}에 위임한다.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<AbstractElementValue> {
		private Serializer() {
			super(AbstractElementValue.class);
		}
		
		@Override
		public void serialize(AbstractElementValue value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			serializeJson(value, gen);
		}
	}

	/**
	 * {@code @type}/{@code value} 형태의 JSON을 {@link ElementValue}로 역직렬화하는 Jackson
	 * deserializer. {@link ElementValues#parseJsonNode(JsonNode)}와 같은 등록부에 위임한다.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends JsonNodeStdDeserializer<ElementValue> {
		/**
		 * {@link ElementValues}의 타입 등록부를 사용하는 deserializer를 생성한다.
		 */
		public Deserializer() {
			super(ElementValue.class, DESERIALIZER);
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
}
