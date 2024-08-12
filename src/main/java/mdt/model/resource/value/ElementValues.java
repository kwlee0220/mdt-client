package mdt.model.resource.value;

import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.DataType;
import mdt.model.DataTypes;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementValues {
	public static Class<? extends SubmodelElementValue> getValueClass(SubmodelElement element) {
		if ( element instanceof Property prop ) {
			return PropertyValue.class;
		}
		else if ( element instanceof SubmodelElementCollection ) {
			return SubmodelElementCollectionValue.class;
		}
		else if ( element instanceof SubmodelElementList ) {
			return SubmodelElementListValue.class;
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
		if ( element instanceof Property prop ) {
			return getPropertyValue(prop);
		}
		else if ( element instanceof SubmodelElementCollection smec ) {
			return getSubmodelElementCollectionValue(smec);
		}
		else if ( element instanceof SubmodelElementList smel ) {
			return getSubmodelElementListValue(smel);
		}
		else if ( element instanceof MultiLanguageProperty mlp ) {
			return getMultiLanguageProperty(mlp);
		}
		else if ( element instanceof Range rg ) {
			return getRange(rg);
		}
		else {
			String msg = String.format("(SubmodelElementValue) type=%s", element.getClass().getSimpleName());
			throw new UnsupportedOperationException(msg);
		}
	}
	
	public static PropertyValue<?> getPropertyValue(Property prop) {
		DataType dtype = DataTypes.fromAas4jDatatype(prop.getValueType());
		if ( dtype == null ) {
			dtype = DataTypes.STRING;
		}
		if ( prop.getValue() != null ) {
			return PropertyValues.fromDataType(dtype, prop.getValue());
		}
		else {
			return null;
		}
	}
	
	public static SubmodelElementCollectionValue getSubmodelElementCollectionValue(SubmodelElementCollection smec) {
		SubmodelElementCollectionValue value = new SubmodelElementCollectionValue();
		FStream.from(smec.getValue())
				.forEach(subElm -> {
					SubmodelElementValue subValue = getValue(subElm);
					if ( subValue != null ) {
						value.addElementValue(subElm.getIdShort(), subValue);
					}
				});
		return value;
	}
	
	public static SubmodelElementListValue getSubmodelElementListValue(SubmodelElementList smel) {
		SubmodelElementListValue value = new SubmodelElementListValue();
		FStream.from(smel.getValue())
				.forEach(elm -> value.addElementValue(getValue(elm)));
		return value;
	}
	
	public static MultiLanguagePropertyValue getMultiLanguageProperty(MultiLanguageProperty prop) {
		MultiLanguagePropertyValue value = new MultiLanguagePropertyValue();
		value.setValue(FStream.from(prop.getValue()).toList());
		return value;
	}
	
	public static RangeValue getRange(Range range) {
		RangeValue value = new RangeValue();
		value.setIdShort(range.getIdShort());
		
		DataType<?> dtype = DataTypes.fromAas4jDatatype(range.getValueType());
		value.setMin(FOption.map(range.getMin(), dtype::parseValueString));
		value.setMax(FOption.map(range.getMax(), dtype::parseValueString));
		
		return value;
	}
}
