package mdt.model.resource.value;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.DataType;
import mdt.model.DataTypes;
import mdt.model.resource.value.PropertyValues.BooleanValue;
import mdt.model.resource.value.PropertyValues.DecimalValue;
import mdt.model.resource.value.PropertyValues.DoubleValue;
import mdt.model.resource.value.PropertyValues.FloatValue;
import mdt.model.resource.value.PropertyValues.IntegerValue;
import mdt.model.resource.value.PropertyValues.LongValue;
import mdt.model.resource.value.PropertyValues.ShortValue;
import mdt.model.resource.value.PropertyValues.StringValue;


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
		DataType<?> dtype = DataTypes.fromAas4jDatatype(prop.getValueType());
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

	public static PropertyValue<?> fromJsonNode(ValueNode node) {
		if ( node instanceof TextNode tn ) {
			return new StringValue(tn.textValue());
		}
		else if ( node instanceof IntNode in ) {
			return new IntegerValue(in.intValue());
		}
		else if ( node instanceof DoubleNode dn ) {
			return new DoubleValue(dn.doubleValue());
		}
		else if ( node instanceof FloatNode fn ) {
			return new FloatValue(fn.floatValue());
		}
		else if ( node instanceof BooleanNode bn ) {
			return new BooleanValue(bn.booleanValue());
		}
		else if ( node instanceof LongNode n ) {
			return new LongValue(n.longValue());
		}
		else if ( node instanceof ShortNode fn ) {
			return new ShortValue(fn.shortValue());
		}
		else if ( node instanceof DecimalNode n ) {
			return new DecimalValue(n.decimalValue());
		}
		else if ( node instanceof BigIntegerNode n ) {
			BigInteger bint = n.bigIntegerValue();
			return new LongValue(bint.longValue());
		}
		else {
			throw new IllegalArgumentException("Unsupported JsonNode type: " + node.getClass());
		}
	}
	
	public static SubmodelElementCollectionValue getSubmodelElementCollectionValue(SubmodelElementCollection smec) {
		SubmodelElementCollectionValue value = new SubmodelElementCollectionValue(smec.getIdShort());
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
		List<SubmodelElementValue> values = FStream.from(smel.getValue())
													.map(ElementValues::getValue)
													.toList();
		return new SubmodelElementListValue(smel.getIdShort(), values);
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
