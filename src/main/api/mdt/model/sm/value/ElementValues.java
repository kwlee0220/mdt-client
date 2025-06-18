package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import utils.KeyValue;
import utils.func.FOption;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExprParser;


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
			return new PropertyValue(prop.getValue());
		}
		else if ( element instanceof SubmodelElementCollection smec ) {
			Map<String,ElementValue> members = FStream.from(smec.getValue())
												.mapToKeyValue(sme -> KeyValue.of(sme.getIdShort(), getValue(sme)))
												.toMap();
			return new ElementCollectionValue(members);
		}
		else if ( element instanceof SubmodelElementList smel ) {
			List<ElementValue> values = FStream.from(smel.getValue())
														.map(ElementValues::getValue)
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
			return new RangeValue(rg.getMin(), rg.getMax());
		}
		else {
			String msg = String.format("(SubmodelElementValue) type=%s", element.getClass().getSimpleName());
			throw new UnsupportedOperationException(msg);
		}
	}

	public static PropertyValue getPropertyValue(Property prop) {
		return new PropertyValue(prop.getValue());
	}
	public static FileValue getFileValue(File aasFile) {
		return new FileValue(aasFile.getContentType(), aasFile.getValue());
	}
	public static MultiLanguagePropertyValue getMLPValue(MultiLanguageProperty mlp) {
		return new MultiLanguagePropertyValue(mlp.getValue());
	}

	public static String toRawString(SubmodelElement sme) {
		ElementValue smev = FOption.map(sme, ElementValues::getValue);
		return toRawString(smev);
	}

	public static String toRawString(ElementValue smev) {
		if ( smev != null ) {
			return ( smev instanceof PropertyValue propv )
					? FOption.getOrElse(propv.get(), "")
					: MDTModelSerDe.toJsonString(smev);
		}
		else {
			return null;
		}
	}

	public static SubmodelElement updateWithRawString(SubmodelElement sme, String rawString) throws IOException {
		rawString = rawString.trim();
		JsonNode rawValue = ( rawString.startsWith("{") )
							? MDTModelSerDe.readJsonNode(rawString)
							: new TextNode(rawString);
		return update(sme, rawValue);
	}
	
	public static SubmodelElement update(SubmodelElement sme, JsonNode valueNode) throws IOException {
		if ( valueNode.isMissingNode() ) {
			return sme;
		}
		
		if ( sme instanceof Property prop ) {
			if ( valueNode.isValueNode()  ) {
				prop.setValue(valueNode.asText());
			}
			else {
				update(prop, MDTModelSerDe.readValue(valueNode, PropertyValue.class));
			}
		}
		else if ( sme instanceof SubmodelElementCollection smc ) {
			FStream.from(smc.getValue())
					.tagKey(v -> v.getIdShort())
					.innerJoin(FStream.from(valueNode.fields()).toKeyValueStream(Entry::getKey, Entry::getValue))
					.forEachOrThrow(match -> update(match.value()._1, match.value()._2));
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			FStream.from(sml.getValue())
					.zipWith(FStream.from(valueNode.elements()))
					.forEachOrThrow(tup -> update(tup._1, tup._2));
		}
		else if ( sme instanceof File aasFile ) {
			update(aasFile, MDTModelSerDe.readValue(valueNode, FileValue.class));
		}
		else if ( sme instanceof Range rg ) {
			update(rg, MDTModelSerDe.readValue(valueNode, RangeValue.class));
		}
		else if ( sme instanceof MultiLanguageProperty mlprop ) {
			update(mlprop, MDTModelSerDe.readValue(valueNode, MultiLanguagePropertyValue.class));
		}
		else {
			String msg = String.format("Unsupported SubmodelElement(%s) for 'update(SubmodelElement, JsonNode)",
										sme.getClass());
			throw new IllegalArgumentException(msg);
		}
		
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
										"Expecing %s, but %s", PropertyValue.class.getName(), smev.getClass().getName());
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
										"Expecing {}, but {}", FileValue.class.getName(), smev.getClass().getName());
			update(aasFile, (FileValue)smev);
		}
		else if ( sme instanceof Range rg ) {
			Preconditions.checkArgument(smev instanceof RangeValue,
										"Expecing {}, but {}", RangeValue.class.getName(), smev.getClass().getName());
			update(rg, (RangeValue)smev);
		}
		else if ( sme instanceof MultiLanguageProperty mlprop ) {
			Preconditions.checkArgument(smev instanceof MultiLanguagePropertyValue,
										"Expecing {}, but {}", MultiLanguagePropertyValue.class.getName(),
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
	
	public static Property update(Property prop, PropertyValue newValue) {
		prop.setValue(newValue.get());
		return prop;
	}
	
	public static File update(File aasFile, FileValue newValue) {
		aasFile.setContentType(newValue.getMimeType());
		aasFile.setValue(newValue.getValue());
		return aasFile;
	}
	
	public static Range update(Range rg, RangeValue newValue) {
		rg.setMin(newValue.getMin());
		rg.setMax(newValue.getMax());
		return rg;
	}
	
	public static MultiLanguageProperty update(MultiLanguageProperty mlprop, MultiLanguagePropertyValue newValue) {
		mlprop.setValue(newValue.getLangTextAll());
		return mlprop;
	}
	
	public static ElementValue parseExpr(String expr) {
		return MDTExprParser.parseValueLiteral(expr).evaluate();
	}
	
	private static final BiMap<String,Class<? extends ElementValue>> TYPE_CLASSES = HashBiMap.create();
	private static final BiMap<Class<? extends ElementValue>, String> CLASS_TYPES = TYPE_CLASSES.inverse();
	static {
		TYPE_CLASSES.put(PropertyValue.SERIALIZATION_TYPE, PropertyValue.class);
		TYPE_CLASSES.put(FileValue.SERIALIZATION_TYPE, FileValue.class);
		TYPE_CLASSES.put(RangeValue.SERIALIZATION_TYPE, RangeValue.class);
		TYPE_CLASSES.put(MultiLanguagePropertyValue.SERIALIZATION_TYPE, MultiLanguagePropertyValue.class);
		TYPE_CLASSES.put(OperationVariableValue.SERIALIZATION_TYPE, OperationVariableValue.class);
		TYPE_CLASSES.put(ReferenceElementValue.SERIALIZATION_TYPE, ReferenceElementValue.class);
		TYPE_CLASSES.put(ElementCollectionValue.SERIALIZATION_TYPE, ElementCollectionValue.class);
		TYPE_CLASSES.put(ElementListValue.SERIALIZATION_TYPE, ElementListValue.class);
	}
	
	public static Class<? extends ElementValue> getElementValueClass(String type) {
		Class<? extends ElementValue> valCls = TYPE_CLASSES.get(type);
		if ( valCls != null ) {
			return valCls;
		}
		else {
			throw new IllegalArgumentException("Unknown ElementValue's type: " + type);
		}
	}
	
	public static String getTypeCode(Class<? extends ElementValue> valueCls) {
		String typeCode = CLASS_TYPES.get(valueCls);
		if ( typeCode != null ) {
			return typeCode;
		}
		else {
			throw new IllegalArgumentException("Unknown ElementValue class: " + valueCls);
		}
	}
	
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ElementValue> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<ElementValue> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(ElementValue value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			value.serialize(gen);
		}
	}
}
