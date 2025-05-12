package mdt.model.sm.ref;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractElementReference implements ElementReference {
	@Override
	public SubmodelElement update(SubmodelElement sme) throws IOException {
		ElementValue newValue = ElementValues.getValue(sme);
		return updateValue(newValue);
	}

	@Override
	public SubmodelElement updateWithValueJsonNode(JsonNode valueNode) throws IOException {
		SubmodelElement sme = read();
		ElementValues.update(sme, valueNode);
		write(sme);
		
		return sme;
	}

	@Override
	public SubmodelElement updateWithValueJsonString(String valueJsonString) throws IOException {
		return updateWithValueJsonNode(MDTModelSerDe.readJsonNode(valueJsonString));
	}

	@Override
	public SubmodelElement updateWithRawString(String externStr) throws IOException {
		externStr = externStr.trim();
		JsonNode rawValue = ( externStr.startsWith("{") )
							? MDTModelSerDe.readJsonNode(externStr)
							: new TextNode(externStr);
		return updateWithValueJsonNode(rawValue);
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.toJsonString(this);
	}

	@Override
	public JsonNode toJsonNode() throws IOException {
		return MDTModelSerDe.toJsonNode(this);
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and casts it to a Property.
	 * <p>
	 * If the referred element is not a Property, throws an IOException.
	 * 
	 * @return	Property referred to by this reference. Or null if the reference is null.
	 * @throws	IOException If the referred element is not a Property
	 */
	public Property readAsProperty() throws IOException {
		SubmodelElement sme = read();
		if ( sme == null ) {
			return null;
		}
		else if ( sme instanceof Property prop ) {
			return prop;
		}
		else {
			throw new IOException("not a Property: element=" + sme);
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as a string.
	 * <p>
	 * If the referred element is not a Property or its value is not a string, throws an IOException.
	 * 
	 * @return a string value of the Property referred to by this reference. Or null if the reference is null.
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 * 						a {@link DataTypeDefXsd#STRING}
	 */
	public String readAsString() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.STRING ) {
			return prop.getValue().toString();
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.STRING, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as an integer.
	 * <p>
	 * If the referred element is not a Property or its value is not an integer, throws an IOException.
	 * 
	 * @return an integer value of the Property referred to by this reference. Or null if the reference is null.
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 * 						a {@link DataTypeDefXsd#INT}
     */
	public Integer readAsInt() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.INT ) {
			return Integer.parseInt(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.INT, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as a {@link BigInteger}.
	 * <p>
	 * If the referred element is not a Property or its valueType is not an {@link DataTypeDefXsd#INTEGER},
	 * throws an IOException.
	 * 
	 * @return a BigInteger value of the Property referred to by this reference. Or null if the reference is null
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 * 			a {@link DataTypeDefXsd#INTEGER}
	 */
	public BigInteger readAsInteger() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.INTEGER ) {
			return new BigInteger(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.INTEGER, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as a long.
	 * <p>
	 * If the referred element is not a Property or its valueType is not a {@link DataTypeDefXsd#LONG},
	 * throws an IOException.
	 * 
	 * @return a long value of the Property referred to by this reference. Or null if the reference is null
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 * 			a {@link DataTypeDefXsd#LONG}
	 */
	public Long readAsLong() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.LONG ) {
			return Long.parseLong(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.LONG, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as a boolean.
	 * <p>
	 * If the referred element is not a Property or its valueType is not a {@link DataTypeDefXsd#BOOLEAN},
	 * throws an IOException.
	 * 
	 * @return a boolean value of the Property referred to by this reference. Or null if the reference is null
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 * 			a {@link DataTypeDefXsd#BOOLEAN}.
	 */
	public Boolean readAsBoolean() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.BOOLEAN ) {
			return Boolean.parseBoolean(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.BOOLEAN, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as a float.
	 * <p>
	 * If the referred element is not a Property or its valueType is not a {@link DataTypeDefXsd#FLOAT},
	 * 
	 * @return a float value of the Property referred to by this reference. Or null if the reference is null
	 * @throws IOException If the referred element is not a Property or its valueType is not
	 *             a {@link DataTypeDefXsd#FLOAT}.
	 */
	public Float readAsFloat() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.FLOAT ) {
			return Float.parseFloat(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.FLOAT, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as
	 * a Duration.
	 * <p>
	 * If the referred element is not a Property or its valueType is not a
	 * {@link DataTypeDefXsd#DURATION}, throws an IOException.
	 * 
	 * @return a double value of the Property referred to by this reference. Or null
	 *         if the reference is null
	 * @throws IOException If the referred element is not a Property or its
	 *                     valueType is not a {@link DataTypeDefXsd#DURATION}.
	 */
	public Duration readAsDuration() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.DURATION ) {
			return Duration.parse(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.DURATION, json));
		}
	}
	
	/**
	 * Reads the SubmodelElement referred to by this reference and gets its value as
	 * a LocalDateTime.
	 * <p>
	 * If the referred element is not a Property or its valueType is not a
	 * {@link DataTypeDefXsd#DATE_TIME}, throws an IOException.
	 * 
	 * @return a double value of the Property referred to by this reference. Or null
	 *        	if the reference is null
	 * @throws IOException If the referred element is not a Property or its
	 * 	               		valueType is not a {@link DataTypeDefXsd#DATE_TIME}.
	 */
	public LocalDateTime readAsDateTime() throws IOException {
		Property prop = readAsProperty();
		if ( prop == null ) {
			return null;
		}
		else if ( prop.getValueType() == DataTypeDefXsd.DATE_TIME ) {
			return LocalDateTime.parse(prop.getValue());
		}
		else {
			String json = MDTModelSerDe.toJsonString(prop);
			throw new IOException(String.format("not a %s Property: prop=%s", DataTypeDefXsd.DATE_TIME, json));
		}
	}
}
