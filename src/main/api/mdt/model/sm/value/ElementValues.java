package mdt.model.sm.value;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;

import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.sm.value.PropertyValue.BooleanPropertyValue;
import mdt.model.sm.value.PropertyValue.DateTimePropertyValue;
import mdt.model.sm.value.PropertyValue.DoublePropertyValue;
import mdt.model.sm.value.PropertyValue.DurationPropertyValue;
import mdt.model.sm.value.PropertyValue.FloatPropertyValue;
import mdt.model.sm.value.PropertyValue.IntegerPropertyValue;
import mdt.model.sm.value.PropertyValue.LongPropertyValue;
import mdt.model.sm.value.PropertyValue.ShortPropertyValue;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementValues {
	/**
	 * 주어진 {@link SubmodelElement}에서 대한 value에 해당하는 부분만을 추출하여
	 * {@link ElementValue} 형태로 반환한다.
	 *
	 * @param element 값을 추출할 {@link SubmodelElement} 객체.
	 * @return 추출된 {@link ElementValue} 객체.
	 */
	public static ElementValue getValue(@Nullable SubmodelElement element) {
		if ( element == null ) {
			return null;
		}
		
		if ( element instanceof Property prop ) {
			return PropertyValue.from(prop);
		}
		else if ( element instanceof SubmodelElementCollection smc ) {
			return ElementCollectionValue.from(smc);
		}
		else if ( element instanceof SubmodelElementList sml ) {
			return ElementListValue.from(sml);
		}
		else if ( element instanceof File aasFile ) {
			return FileValue.from(aasFile);
		}
		else if ( element instanceof MultiLanguageProperty mlp ) {
			return MultiLanguagePropertyValue.from(mlp);
		}
		else if ( element instanceof Range rg ) {
			return RangeValue.from(rg);
		}
		else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'getValue(SubmodelElement)'",
										element.getClass());
			throw new IllegalArgumentException(msg);
		}
	}
	
	/**
	 * {@link SubmodelElement} proto를 참조하여 주어진 값 객체(vo)를 파싱하여 {@link ElementValue}
	 * 객체로 반환한다.
	 *
	 * @param vo    파싱할 값 객체.
	 * @param proto 참조할 {@link SubmodelElement} 객체.
	 * @return 파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 값 객체가 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue fromValueObject(Object vo, SubmodelElement proto) throws IOException {
		if ( proto instanceof Property prop ) {
			return PropertyValue.fromValueObject(vo, prop);
		}
		else if ( proto instanceof SubmodelElementCollection smc ) {
			return ElementCollectionValue.fromValueObject(vo, smc);
		}
		else if ( proto instanceof SubmodelElementList sml ) {
			return ElementListValue.fromValueObject(vo, sml);
		}
		else if ( proto instanceof File aasFile ) {
			return FileValue.fromValueObject(vo, aasFile);
		}
		else if ( proto instanceof MultiLanguageProperty mlp ) {
			return MultiLanguagePropertyValue.fromValueObject(vo, mlp);
		}
		else if ( proto instanceof Range rg ) {
			return RangeValue.fromValueObject(vo, rg);
		}
		else if ( proto instanceof ReferenceElement ref ) {
			return ReferenceElementValue.fromValueObject(vo, ref);
		}
        else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'fromValueObject(Object, SubmodelElement)'",
										proto.getClass());
			throw new IllegalArgumentException(msg);
        }
	}
	
	/**
	 * {@link SubmodelElement} proto를 참조하여 주어진 JSON 노드(vnode)를 파싱하여
	 * {@link ElementValue} 객체로 반환한다.
	 *
	 * @param vnode	파싱할 JSON 노드.
	 * @param proto	참조할 {@link SubmodelElement} 객체.
	 * @return	파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 JSON 노드가 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue parseValueJsonNode(JsonNode vnode, SubmodelElement proto) throws IOException {
		if ( proto instanceof Property prop ) {
			return PropertyValue.parseValueJsonNode(vnode, prop);
		}
		else if ( proto instanceof SubmodelElementCollection smc ) {
			return ElementCollectionValue.parseValueJsonNode(vnode, smc);
		}
		else if ( proto instanceof SubmodelElementList sml ) {
			return ElementListValue.parseValueJsonNode(vnode, sml);
		}
		else if ( proto instanceof File aasFile ) {
			return FileValue.parseValueJsonNode(vnode, aasFile);
		}
		else if ( proto instanceof MultiLanguageProperty mlp ) {
			return MultiLanguagePropertyValue.parseValueJsonNode(vnode, mlp);
		}
		else if ( proto instanceof Range rg ) {
			return RangeValue.parseValueJsonNode(vnode, rg);
		}
		else if ( proto instanceof ReferenceElement ref ) {
			return ReferenceElementValue.parseValueJsonNode(vnode, ref);
		}
        else {
			String msg = String.format(
					"Unsupported SubmodelElement(%s) for 'parseValueJsonNode(JsonNode, SubmodelElement)'",
					proto.getClass());
			throw new IllegalArgumentException(msg);
        }
	}
	
	/**
	 * {@link SubmodelElement} proto를 참조하여 주어진 JSON 문자열(valueJsonString)을 파싱하여
	 * {@link ElementValue} 객체로 반환한다.
	 *
	 * @param valueJsonString 파싱할 JSON 문자열.
	 * @param proto           참조할 {@link SubmodelElement} 객체.
	 * @return 파싱된 {@link ElementValue} 객체.
	 * @throws IOException 입력 JSON 문자열이 참조 proto와 맞지 않는 경우 발생.
	 */
	public static ElementValue parseValueJsonString(String valueJsonString, SubmodelElement proto) throws IOException {
		JsonNode jnode = MDTModelSerDe.getJsonMapper().readTree(valueJsonString);
		return parseValueJsonNode(jnode, proto);
	}
	
	/**
	 * 주어진 {@link SubmodelElement} sme 객체에 대한 값을 smev로 갱신한다.
	 * 
	 * @param sme	갱신할 {@link SubmodelElement} 객체.
	 * @param smev	갱신할 값.
	 */
	public static SubmodelElement update(SubmodelElement sme, ElementValue smev) {
		if ( sme instanceof Property prop ) {
			Preconditions.checkArgument(smev instanceof PropertyValue,
										"Expecting %s, but %s", PropertyValue.class.getName(), smev.getClass().getName());
			((PropertyValue)smev).update(prop);
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
			((RangeValue)smev).update(rg);
		}
		else if ( sme instanceof MultiLanguageProperty mlprop ) {
			Preconditions.checkArgument(smev instanceof MultiLanguagePropertyValue,
										"Expecting %s, but %s", MultiLanguagePropertyValue.class.getName(),
																smev.getClass().getName());
			((MultiLanguagePropertyValue)smev).update(mlprop);
		}
		else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'update(SubmodelElement, JsonNode)",
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
		ElementValue value = parseValueJsonNode(valueNode, sme);
		update(sme, value);
		
		return sme;
	}
	
	public static void updateWithValueJsonString(SubmodelElement target, String valueJsonString) throws IOException {
		ElementValue smev = parseValueJsonString(valueJsonString, target);
		update(target, smev);
	}
	
	public static ElementValue parseExpr(String expr) {
		return MDTExpressionParser.parseValueLiteral(expr).evaluate();
	}
	
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

	private static final String FIELD_TYPE = "@type";
	private static final String FIELD_VALUE = "value";
	public static ElementValue parseJsonNode(JsonNode jnode) {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new UncheckedIOException(null, new IOException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode)));
		}
		
		JsonNode valueNode = JacksonUtils.getFieldOrNull(jnode, FIELD_VALUE);
		switch ( type ) {
			case StringPropertyValue.SERIALIZATION_TYPE:
				return StringPropertyValue.deserializeValue(valueNode);
			case IntegerPropertyValue.SERIALIZATION_TYPE:
				return IntegerPropertyValue.deserializeValue(valueNode);
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
			case ElementListValue.SERIALIZATION_TYPE:
				return ElementListValue.deserializeValue(valueNode);
			case ReferenceElementValue.SERIALIZATION_TYPE:
				return ReferenceElementValue.deserializeValue(valueNode);
			default:
				throw new JacksonDeserializationException("Unregistered ElementValue type: " + type);
		}
	}
	
	public static ElementValue parseJsonString(String json) throws IOException {
		JsonNode jnode = MDTModelSerDe.readJsonNode(json);
		return parseJsonNode(jnode);
	}
	
	public static void serializeJson(AbstractElementValue value, JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_TYPE, value.getSerializationType());
		gen.writeFieldName(FIELD_VALUE);
		value.serializeValue(gen);
		gen.writeEndObject();
	}
	
	public static String unquote(String str) {
		if (str == null) return null;
		str = str.trim();
		if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length() - 1);
		}
		return str;
	}
	
//	private String quote(String value) {
//		if ( value != null ) {
//			return "\"" + value + "\"";
//		}
//		else {
//			return "null";
//		}
//	}
}
