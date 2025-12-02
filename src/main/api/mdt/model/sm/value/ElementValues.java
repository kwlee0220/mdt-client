package mdt.model.sm.value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
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
import com.google.common.collect.Maps;

import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
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
	public static ElementValue getValue(SubmodelElement element) {
		if ( element == null ) {
			return null;
		}
		
		if ( element instanceof Property prop ) {
			return getPropertyValue(prop);
		}
		else if ( element instanceof SubmodelElementCollection smec ) {
			Map<String,ElementValue> members = Maps.newHashMap();
			for ( SubmodelElement field : smec.getValue() ) {
				ElementValue fieldValue = getValue(field);
				if ( fieldValue != null ) {
					members.put(field.getIdShort(), fieldValue);
				}
			}
			return new ElementCollectionValue(members);
		}
		else if ( element instanceof SubmodelElementList smel ) {
			List<ElementValue> values = FStream.from(smel.getValue())
														.mapOrThrow(ElementValues::getValue)
														.toList();
			return new ElementListValue(values);
		}
		else if ( element instanceof File aasFile ) {
			return getFileValue(aasFile);
		}
		else if ( element instanceof MultiLanguageProperty mlp ) {
			return getMLPValue(mlp);
		}
		else if ( element instanceof Range rg ) {
			DataType<?> vtype = DataTypes.fromAas4jDatatype(rg.getValueType());		
			Object min = vtype.parseValueString(rg.getMin());
			Object max = vtype.parseValueString(rg.getMax());
	
			return new RangeValue(vtype, min, max);
		}
		else {
			String msg = String.format("(SubmodelElementValue) type=%s", element.getClass().getSimpleName());
			throw new UnsupportedOperationException(msg);
		}
	}

	public static PropertyValue<?> getPropertyValue(Property prop) {
		return PropertyValue.from(prop);
	}
	public static FileValue getFileValue(File aasFile) {
		return new FileValue(aasFile.getValue(), aasFile.getContentType());
	}
	public static MultiLanguagePropertyValue getMLPValue(MultiLanguageProperty mlp) {
		return new MultiLanguagePropertyValue(mlp.getValue());
	}
	
	public static ElementValue parseValueJsonString(SubmodelElement proto, String valueJsonString) throws IOException {
		JsonNode jnode = MDTModelSerDe.getJsonMapper().readTree(valueJsonString);
		return parseValueJsonNode(proto, jnode);
	}
	
	public static ElementValue parseValueJsonNode(SubmodelElement proto, JsonNode vnode) throws IOException {
		switch ( proto ) {
			case Property prop:
				return PropertyValue.parseValueJsonNode(prop, vnode);
			case SubmodelElementCollection smc:
				return ElementCollectionValue.parseValueJsonNode(smc, vnode);
			case SubmodelElementList sml:
				return ElementListValue.parseValueJsonNode(sml, vnode);
			case File aasFile:
				return FileValue.parseValueJsonNode(aasFile, vnode);
			case MultiLanguageProperty mlp:
				return MultiLanguagePropertyValue.parseValueJsonNode(mlp, vnode);
			case Range rg:
				return RangeValue.parseValueJsonNode(rg, vnode);
			case ReferenceElement ref:
				return ReferenceElementValue.parseValueJsonNode(ref, vnode);
			default:
				String msg = String.format(
						"Unsupported SubmodelElement(%s) for 'parseValueString(SubmodelElement, String)'",
						proto.getClass());
				throw new IllegalArgumentException(msg);
		}
	}
	
	public static SubmodelElement update(@NonNull SubmodelElement sme, JsonNode valueNode) throws IOException {
		ElementValue value = parseValueJsonNode(sme, valueNode);
		update(sme, value);
		
		return sme;
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
			update(prop, (PropertyValue)smev);
		}
		else if ( sme instanceof SubmodelElementCollection smc ) {
			if ( smev instanceof ElementCollectionValue smcv ) {
				FStream.from(smc.getValue())
						.tagKey(v -> v.getIdShort())
						.innerJoin(KeyValueFStream.from(smcv.getFieldAll()))
						.forEach(match -> update(match.value()._1, match.value()._2));
			}
			else {
				String msg = String.format("Expecting %s, but %s",
										ElementCollectionValue.class.getName(), smev.getClass().getName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			if ( smev instanceof ElementListValue smlv ) {
				FStream.from(sml.getValue())
						.zipWith(FStream.from(smlv.getElementAll()))
						.forEach(match -> update(match._1, match._2));
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
			update(aasFile, (FileValue)smev);
		}
		else if ( sme instanceof Range rg ) {
			Preconditions.checkArgument(smev instanceof RangeValue,
										"Expecting %s, but %s", RangeValue.class.getName(), smev.getClass().getName());
			update(rg, (RangeValue)smev);
		}
		else if ( sme instanceof MultiLanguageProperty mlprop ) {
			Preconditions.checkArgument(smev instanceof MultiLanguagePropertyValue,
										"Expecting %s, but %s", MultiLanguagePropertyValue.class.getName(),
																smev.getClass().getName());
			update(mlprop, (MultiLanguagePropertyValue)smev);
		}
		else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'update(SubmodelElement, JsonNode)",
										sme.getClass());
			throw new IllegalArgumentException(msg);
		}
		
		return sme;
	}
	
	public static Property update(Property prop, @NonNull PropertyValue<?> newValue) {
		String value = newValue.getDataType().toValueString(newValue.get());
		prop.setValue(value);
		return prop;
	}
	
	public static File update(File aasFile, FileValue newValue) {
		aasFile.setContentType(newValue.getMimeType());
		aasFile.setValue(newValue.getValue());
		return aasFile;
	}
	
	public static Range update(Range rg, RangeValue<?> newValue) {
		DataType<?> vtype = newValue.getValueType();
		rg.setMin(vtype.toValueString(newValue.getMin()));
		rg.setMax(vtype.toValueString(newValue.getMax()));
		return rg;
	}
	
	public static MultiLanguageProperty update(MultiLanguageProperty mlprop, MultiLanguagePropertyValue newValue) {
		mlprop.setValue(newValue.getLangTextAll());
		return mlprop;
	}
	
	public static void updateWithValueJsonString(SubmodelElement target, String valueJsonString) throws IOException {
		ElementValue smev = parseValueJsonString(target, valueJsonString);
		update(target, smev);
	}
	
	public static ElementValue parseExpr(String expr) {
		return MDTExpressionParser.parseValueLiteral(expr).evaluate();
	}
	
//	private static final BiMap<String,Class<? extends ElementValue>> TYPE_CLASSES = HashBiMap.create();
//	static {
//		TYPE_CLASSES.put(StringPropertyValue.SERIALIZATION_TYPE, StringPropertyValue.class);
//		TYPE_CLASSES.put(IntegerPropertyValue.SERIALIZATION_TYPE, IntegerPropertyValue.class);
//		TYPE_CLASSES.put(DoublePropertyValue.SERIALIZATION_TYPE, DoublePropertyValue.class);
//		TYPE_CLASSES.put(FloatPropertyValue.SERIALIZATION_TYPE, FloatPropertyValue.class);
//		TYPE_CLASSES.put(BooleanPropertyValue.SERIALIZATION_TYPE, BooleanPropertyValue.class);
//		TYPE_CLASSES.put(DateTimePropertyValue.SERIALIZATION_TYPE, DateTimePropertyValue.class);
//		TYPE_CLASSES.put(DurationPropertyValue.SERIALIZATION_TYPE, DurationPropertyValue.class);
//		TYPE_CLASSES.put(FileValue.SERIALIZATION_TYPE, FileValue.class);
//		TYPE_CLASSES.put(RangeValue.SERIALIZATION_TYPE, RangeValue.class);
//		TYPE_CLASSES.put(MultiLanguagePropertyValue.SERIALIZATION_TYPE, MultiLanguagePropertyValue.class);
//		TYPE_CLASSES.put(OperationVariableValue.SERIALIZATION_TYPE, OperationVariableValue.class);
//		TYPE_CLASSES.put(ReferenceElementValue.SERIALIZATION_TYPE, ReferenceElementValue.class);
//		TYPE_CLASSES.put(ElementCollectionValue.SERIALIZATION_TYPE, ElementCollectionValue.class);
//		TYPE_CLASSES.put(ElementListValue.SERIALIZATION_TYPE, ElementListValue.class);
//	}
//	
//	public static Class<? extends ElementValue> getElementValueClass(String type) {
//		Class<? extends ElementValue> valCls = TYPE_CLASSES.get(type);
//		if ( valCls != null ) {
//			return valCls;
//		}
//		else {
//			throw new IllegalArgumentException("Unknown ElementValue's type: " + type);
//		}
//	}
	
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
