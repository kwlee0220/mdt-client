package mdt.task;

import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;

import mdt.model.AASUtils;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.resource.value.ElementValues;
import mdt.model.resource.value.PropertyValue;
import mdt.model.resource.value.PropertyValues;
import mdt.model.resource.value.PropertyValues.BooleanValue;
import mdt.model.resource.value.PropertyValues.DoubleValue;
import mdt.model.resource.value.PropertyValues.FloatValue;
import mdt.model.resource.value.PropertyValues.IntegerValue;
import mdt.model.resource.value.PropertyValues.ShortValue;
import mdt.model.resource.value.PropertyValues.StringValue;
import mdt.model.workflow.descriptor.port.PortDirection;
import mdt.model.resource.value.SubmodelElementCollectionValue;
import mdt.model.resource.value.SubmodelElementValue;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementPort implements Port {
	private final String m_name;
	private final PortDirection m_direction;
	private final SubmodelElementReference m_ref;
	private final boolean m_valueOnly;
	
	SubmodelElementPort(String name, PortDirection type, SubmodelElementReference ref, boolean valueOnly) {
		m_direction = type;
		m_name = name;
		m_ref = ref;
		m_valueOnly = valueOnly;
	}

	@Override
	public String getName() {
		return m_name;
	}
	
	@Override
	public PortDirection getDirection() {
		return m_direction;
	}

	@Override
	public boolean isValuePort() {
		return m_valueOnly;
	}

	public SubmodelElementReference getReference() {
		return m_ref;
	}

	public SubmodelElement getSubmodelElement() {
		return m_ref.get();
	}
	
	@Override
	public String getJsonString() {
		return AASUtils.writeJson(getAsJsonObject());
	}
	
	@Override
	public JsonNode getJsonNode() {
		return AASUtils.getJsonSerializer().toNode(getAsJsonObject());
	}
	
	public Object getAsJsonObject() {
		SubmodelElement sme = getSubmodelElement();
		if ( isValuePort() ) {
			return ElementValues.getValue(sme).toJsonObject();
		}
		else {
			return sme;
		}
	}

	public Object getRawValue() {
		SubmodelElement sme = getSubmodelElement();
		if ( isValuePort() ) {
			SubmodelElementValue smev = ElementValues.getValue(sme);
			if ( smev instanceof PropertyValue<?> prop ) {
				return prop.get();
			}
			else {
				return smev;
			}
		}
		else {
			return sme;
		}
	}

	@Override
	public void setJsonNode(JsonNode node) {
		if ( isValuePort() ) {
			if ( node instanceof TextNode tn ) {
				setSubmodelElementValue(new StringValue(tn.textValue()));
			}
			else if ( node instanceof IntNode in ) {
				setSubmodelElementValue(new IntegerValue(in.intValue()));
			}
			else if ( node instanceof DoubleNode dn ) {
				setSubmodelElementValue(new DoubleValue(dn.doubleValue()));
			}
			else if ( node instanceof FloatNode fn ) {
				setSubmodelElementValue(new FloatValue(fn.floatValue()));
			}
			else if ( node instanceof BooleanNode bn ) {
				setSubmodelElementValue(new BooleanValue(bn.booleanValue()));
			}
			else if ( node instanceof ShortNode fn ) {
				setSubmodelElementValue(new ShortValue(fn.shortValue()));
			}
			else {
				throw new IllegalArgumentException("Unsupported JsonNode type: " + node.getClass());
			}
		}
		else {
			SubmodelElement sme = AASUtils.readJson(node, SubmodelElement.class);
			setSubmodelElement(sme);
		}
	}

	@Override
	public void setJsonString(String jsonString) {
		if ( isValuePort() ) {
			jsonString = jsonString.trim();
			if ( jsonString.startsWith("{") ) {
				SubmodelElement sme = m_ref.get();
				if ( sme instanceof Property prop ) {
					PropertyValue<?> smev = AASUtils.readJson(jsonString, PropertyValue.class);
					setSubmodelElementValue(smev);
				}
				else if ( sme instanceof SubmodelElementCollection ) {
					SubmodelElementCollectionValue smcv = AASUtils.readJson(jsonString,
																			SubmodelElementCollectionValue.class);
					setSubmodelElementValue(smcv);
				}
				else {
					throw new IllegalArgumentException("Unsupported SubmodelElementValue: " + sme.getClass());
				}
			}
			else {
				setSubmodelElementValue(new StringValue(jsonString));
			}
		}
		else {
			SubmodelElement sme = AASUtils.readJson(jsonString, SubmodelElement.class);
			setSubmodelElement(sme);
		}
	}

	public void setSubmodelElement(SubmodelElement sme) {
		Preconditions.checkState(!isInputPort());
		
		if ( isValuePort() ) {
			m_ref.set(ElementValues.getValue(sme));
		}
		else {
			m_ref.set(sme);
		}
	}

	public void setSubmodelElementValue(SubmodelElementValue value) {
		Preconditions.checkState(!isInputPort());
		
		m_ref.set(value);
	}
	
	public void set(Object value) {
		if ( value instanceof SubmodelElementValue smev ) {
			setSubmodelElementValue(smev);
		}
		else if ( value instanceof SubmodelElement sme ) {
			setSubmodelElement(sme);
		}
		else if ( value instanceof String str ) {
			setJsonString(str);
		}
		else if ( Primitives.isWrapperType(value.getClass()) ) {
			setSubmodelElementValue(PropertyValues.fromValue(value));
		}
		else {
			throw new IllegalArgumentException("Unexpected Value: " + value);
		}
	}
	
	@Override
	public String toString() {
		return String.format("[%s(%s)] %s", m_name, m_direction.getTypeString(), m_ref.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		SubmodelElementPort other = (SubmodelElementPort)obj;
		return Objects.equals(m_name, other.m_name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_name);
	}
}