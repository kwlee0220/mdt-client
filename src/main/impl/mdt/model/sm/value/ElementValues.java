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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;

import utils.KeyValue;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementValues {
	public static Class<? extends SubmodelElementValue> getValueClass(SubmodelElement element) {
		if ( element instanceof Property ) {
			return PropertyValue.class;
		}
		else if ( element instanceof SubmodelElementCollection ) {
			return SubmodelElementCollectionValue.class;
		}
		else if ( element instanceof SubmodelElementList ) {
			return SubmodelElementListValue.class;
		}
		else if ( element instanceof File ) {
			return FileValue.class;
		}
		else if ( element instanceof MultiLanguageProperty ) {
			return MultiLanguagePropertyValue.class;
		}
		else if ( element instanceof Range ) {
			return RangeValue.class;
		}
		else {
			String msg = String.format("(SubmodelElementValue) type=%s", element.getClass().getSimpleName());
			throw new UnsupportedOperationException(msg);
		}
	}
	
	public static SubmodelElementValue getValue(SubmodelElement element) {
		if ( element == null ) {
			return null;
		}
		if ( element instanceof Property prop ) {
			return new PropertyValue(prop.getValue());
		}
		else if ( element instanceof SubmodelElementCollection smec ) {
			Map<String, SubmodelElementValue> members
								= FStream.from(smec.getValue())
										.mapToKeyValue(member -> KeyValue.of(member.getIdShort(), getValue(member)))
										.toMap();
			return new SubmodelElementCollectionValue(members);
		}
		else if ( element instanceof SubmodelElementList smel ) {
			List<SubmodelElementValue> values = FStream.from(smel.getValue())
														.map(ElementValues::getValue)
														.toList();
			return new SubmodelElementListValue(values);
		}
		else if ( element instanceof File aasFile ) {
			return new FileValue(aasFile.getContentType(), aasFile.getValue());
		}
		else if ( element instanceof MultiLanguageProperty mlp ) {
			return new MultiLanguagePropertyValue(mlp.getValue());
		}
		else if ( element instanceof Range rg ) {
			return new RangeValue(rg.getMin(), rg.getMax());
		}
		else {
			String msg = String.format("(SubmodelElementValue) type=%s", element.getClass().getSimpleName());
			throw new UnsupportedOperationException(msg);
		}
	}

	public static String toExternalString(SubmodelElement sme) {
		SubmodelElementValue smev = FOption.map(sme, ElementValues::getValue);
		return toExternalString(smev);
	}

	public static String toExternalString(SubmodelElementValue smev) {
		if ( smev != null ) {
			return ( smev instanceof PropertyValue propv )
					? FOption.getOrElse(propv.getValue(), "")
					: MDTModelSerDe.toJsonString(smev);
		}
		else {
			return null;
		}
	}

	public static SubmodelElement updateWithExternalString(SubmodelElement sme, String externStr) throws IOException {
		externStr = externStr.trim();
		JsonNode rawValue = ( externStr.startsWith("{") )
							? MDTModelSerDe.readJsonNode(externStr)
							: new TextNode(externStr);
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
					.innerJoin(FStream.from(valueNode.fields()), SubmodelElement::getIdShort, Entry::getKey)
					.forEachOrThrow(match -> update(match._1, match._2.getValue()));
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
	
	public static SubmodelElement update(SubmodelElement sme, SubmodelElementValue smev) {
		if ( sme instanceof Property prop ) {
			Preconditions.checkArgument(smev instanceof PropertyValue,
										"Expecing {}, but {}", PropertyValue.class.getName(), smev.getClass().getName());
			update(prop, (PropertyValue)smev);
		}
		else if ( sme instanceof SubmodelElementCollection smc ) {
			if ( smev instanceof SubmodelElementCollectionValue smcv ) {
				FStream.from(smc.getValue())
						.innerJoin(FStream.from(smcv.get().entrySet()), SubmodelElement::getIdShort, Entry::getKey)
						.forEach(match -> update(match._1, match._2.getValue()));
			}
			else {
				String msg = String.format("Expecting %s, but %s",
										SubmodelElementCollectionValue.class.getName(), smev.getClass().getName());
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( sme instanceof SubmodelElementList sml ) {
			if ( smev instanceof SubmodelElementListValue smlv ) {
				FStream.from(sml.getValue())
						.zipWith(FStream.from(smlv.get()))
						.forEach(match -> update(match._1, match._2));
			}
			else {
				String msg = String.format("Expecting %s, but %s",
											SubmodelElementListValue.class.getName(), smev.getClass().getName());
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
		prop.setValue(newValue.getValue());
		return prop;
	}
	
	public static File update(File aasFile, FileValue newValue) {
		aasFile.setContentType(newValue.getContentType());
		aasFile.setValue(newValue.getValue());
		return aasFile;
	}
	
	public static Range update(Range rg, RangeValue newValue) {
		rg.setMin(newValue.getMin());
		rg.setMax(newValue.getMax());
		return rg;
	}
	
	public static MultiLanguageProperty update(MultiLanguageProperty mlprop, MultiLanguagePropertyValue newValue) {
		mlprop.setValue(newValue.get());
		return mlprop;
	}
}
