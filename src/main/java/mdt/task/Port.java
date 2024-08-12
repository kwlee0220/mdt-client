package mdt.task;

import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;

import utils.InternalException;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.resource.value.ElementValues;
import mdt.model.resource.value.PropertyValue;
import mdt.model.resource.value.PropertyValues;
import mdt.model.resource.value.PropertyValues.StringValue;
import mdt.model.resource.value.SubmodelElementValue;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class Port {
	private static final Class<? extends SubmodelElementValue> DEFAULT_VALUE_CLASS = PropertyValue.class;
	
	private final String m_name;
	private final PortType m_type;
	private final SubmodelElementReference m_ref;
	
	public static Port from(MDTInstanceManager manager, String argName, String argValue) {
		PortType type = PortType.fromArgName(argName);
		if ( type == null ) {
			throw new IllegalArgumentException("Not Port argument name: " + argName);
		}
		
		int prefixLen = type.getArgNamePrefix().length();
		
		String name = argName.substring(prefixLen);
		SubmodelElementReference smeRef = SubmodelElementReference.parseString(manager, argValue);
		return new Port(type, name, smeRef);
	}
	
	private Port(PortType type, String name, SubmodelElementReference ref) {
		m_type = type;
		m_name = name;
		m_ref = ref;
	}
	
	public static boolean isPortOptionName(String argName) {
		return PortType.fromArgName(argName) != null;
	}
	
	public PortType getType() {
		return m_type;
	}
	
	public boolean isInputPort() {
		return m_type == PortType.INPUT_VALUE || m_type == PortType.INPUT_SME;
	}
	public boolean isOutputPort() {
		return m_type == PortType.OUTPUT_VALUE || m_type == PortType.OUTPUT_SME;
	}
	public boolean isInoutPort() {
		return m_type == PortType.INOUT_VALUE || m_type == PortType.INOUT_SME;
	}
	
	public boolean isValuePort() {
		return m_type == PortType.INPUT_VALUE
				|| m_type == PortType.OUTPUT_VALUE
				|| m_type == PortType.INOUT_VALUE;
	}
	public boolean isSubmodelElementPort() {
		return m_type == PortType.INPUT_SME
				|| m_type == PortType.OUTPUT_SME
				|| m_type == PortType.INOUT_SME;
	}
	
	public String getName() {
		return m_name;
	}
	
	public SubmodelElementReference getReference() {
		return m_ref;
	}
	
	public SubmodelElement getSubmodelElement() {
		return m_ref.get();
	}
	
	// either: SubmodelElement or SubmodelElementValue
	public Object get() {
		SubmodelElement sme = m_ref.get();
		return isSubmodelElementPort() ? sme : ElementValues.getValue(sme);
	}
	
	public Object getAsJsonObject() {
		Object value = get();
		if ( value instanceof SubmodelElementValue smev ) {
			return smev.toJsonObject();
		}
		else {
			return value;
		}
	}
	
	public String getAsJsonString() {
		return AASUtils.writeJson(getAsJsonObject());
	}
	
	public Object getRawValue() {
		Object value = get();
		if ( value instanceof SubmodelElement smev ) {
			return value;
		}
		else if ( value instanceof PropertyValue<?> prop ) {
			return prop.get();
		}
		else {
			throw new InternalException("Unsupport Port value: " + value);
		}
	}
	
	public void set(SubmodelElement sme) {
		Preconditions.checkState(!isInputPort());
		
		if ( isSubmodelElementPort() ) {
			m_ref.set(sme);
		}
		else {
			m_ref.set(ElementValues.getValue(sme));
		}
	}
	
	public void set(SubmodelElementValue value) {
		Preconditions.checkState(!isInputPort());
		
		m_ref.set(value);
	}
	
	public void setJson(String jsonString) {
		if ( isSubmodelElementPort() ) {
			SubmodelElement sme = AASUtils.readJson(jsonString, SubmodelElement.class);
			set(sme);
		}
		else {
			jsonString = jsonString.trim();
			if ( jsonString.startsWith("{") ) {
				PropertyValue<?> smev = AASUtils.readJson(jsonString, PropertyValue.class);
				set(smev);
			}
			else {
				set(new StringValue(jsonString));
			}
		}
	}
	
	public void set(Object value) {
		if ( value instanceof SubmodelElementValue smev ) {
			set(smev);
		}
		else if ( value instanceof SubmodelElement sme ) {
			set(sme);
		}
		else if ( value instanceof String str ) {
			str = str.trim();
			if ( str.startsWith("{") ) {
				PropertyValue<?> smev = AASUtils.readJson(str, PropertyValue.class);
				set(smev);
			}
			else {
				PropertyValue<?> propv = (PropertyValue<?>)get();
				propv.setString(str);
			}
		}
		else if ( Primitives.isWrapperType(value.getClass()) ) {
			set(PropertyValues.fromValue(value));
		}
		else {
			throw new IllegalArgumentException("Unexpected Value: " + value);
		}
	}
	
	@Override
	public String toString() {
		return String.format("[%s(%s)] %s", m_name, m_type.getArgNamePrefix(), m_ref.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		Port other = (Port)obj;
		return Objects.equals(m_name, other.m_name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_name);
	}
}