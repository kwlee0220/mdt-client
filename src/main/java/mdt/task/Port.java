package mdt.task;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.primitives.Primitives;

import mdt.model.AASUtils;
import mdt.model.resource.value.ElementValues;
import mdt.model.resource.value.PropertyValues;
import mdt.model.resource.value.SubmodelElementValue;
import mdt.model.workflow.descriptor.port.PortDirection;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public interface Port {
	/**
	 * 포트에게 부여된 이름을 반환한다.
	 * 
	 * @return	포트 이름.
	 */
	public String getName();
	
	/**
	 * 포트의 타입을 반환한다.
	 * 
	 * @return	포트 타입
	 */
	public PortDirection getDirection();
	
	public default boolean isInputPort() {
		return getDirection() == PortDirection.INPUT;
	}
	public default boolean isOutputPort() {
		return getDirection() == PortDirection.OUTPUT;
	}

	/**
	 * ValueOnly port 여부를 반환한다.
	 * 
	 * @return	ValueOnly port 여부.
	 */
	public boolean isValuePort();
	
	public JsonNode getJsonNode();
	public void setJsonNode(JsonNode node);
	
	public String getJsonString();
	public void setJsonString(String jsonStr);
	
	public default SubmodelElement getSubmodelElement() {
		try {
			JsonDeserializer deser = AASUtils.getJsonDeserializer();
			
			JsonNode node = getJsonNode();
			return deser.read(node, SubmodelElement.class);
		}
		catch ( DeserializationException e ) {
			throw new IllegalStateException("Failed to read SubmodelElement: node=" + getJsonNode());
		}
	}
	
	public default Object getRawValue() {
		JsonNode node = getJsonNode();
		if ( node == null ) {
			return null;
		}
		else if ( node instanceof ValueNode v ) {
			return ElementValues.fromJsonNode(v).get();
		}
		else if ( node instanceof ObjectNode on ) {
			try {
				JsonDeserializer deser = AASUtils.getJsonDeserializer();
				JsonNode typeField = on.get("modelType");
				if ( typeField != null && typeField.isTextual() ) {
					return deser.read(on, SubmodelElement.class);
				}
				else {
					return deser.read(on, SubmodelElementValue.class);
				}
			}
			catch ( DeserializationException e ) {
				throw new IllegalStateException("Failed to read SubmodelElement: node=" + getJsonNode());
			}
		}
		else {
			throw new IllegalArgumentException("Invalid Json object: " + node);
		}
	}
	

	public default String getAsJsonValueString() {
		JsonNode node = getJsonNode();
		if ( node == null ) {
			return null;
		}
		else if ( node instanceof ValueNode v ) {
			return ElementValues.fromJsonNode(v).get().toString();
		}
		else if ( node instanceof ObjectNode on ) {
			try {
				ObjectMapper mapper = AASUtils.getJsonMapper()
												.enable(SerializationFeature.INDENT_OUTPUT);
				return mapper.writeValueAsString(on);
			}
			catch ( JsonProcessingException e ) {
				throw new IllegalStateException("Failed to serialize SubmodelElement to Json: node=" + getJsonNode());
			}
		}
		else {
			throw new IllegalArgumentException("Invalid Json object: " + node);
		}
	}
	
	public default void set(Object value) {
		JsonSerializer ser = AASUtils.getJsonSerializer();
		
		try {
			if ( value instanceof SubmodelElementValue smev ) {
				setJsonString(ser.write(smev));
			}
			else if ( value instanceof SubmodelElement sme ) {
				setJsonString(ser.write(sme));
			}
			else if ( value instanceof String str ) {
				setJsonString(str);
			}
			else if ( Primitives.isWrapperType(value.getClass()) ) {
				setJsonString(ser.write(PropertyValues.fromValue(value)));
			}
			else {
				throw new IllegalArgumentException("Unexpected Value: " + value);
			}
		}
		catch ( SerializationException e ) {
			throw new IllegalArgumentException("Invalid Json object: " + value + ", cause=" + e);
		}
	}
	
//	public boolean isValuePort();
//	
//	public SubmodelElement getSubmodelElement();
//	public void setSubmodelElement(SubmodelElement sme);
//	public void setSubmodelElementValue(SubmodelElementValue smev);
//	
//	// either: SubmodelElement or SubmodelElementValue
//	public default Object get() {
//		SubmodelElement sme = getSubmodelElement();
//		return isValuePort() ? ElementValues.getValue(sme) : sme;
//	}
//	
//	public default Object getAsJsonObject() {
//		SubmodelElement sme = getSubmodelElement();
//		if ( isValuePort() ) {
//			return ElementValues.getValue(sme).toJsonObject();
//		}
//		else {
//			return sme;
//		}
//	}
//	
//	public default String getAsJsonString() {
//		return AASUtils.writeJson(getAsJsonObject());
//	}
	
//	public default String getAsJsonValueString() {
//		Object value = get();
//		if ( value instanceof PropertyValue<?> propv ) {
//			return FOption.getOrElse(propv.get().toString(), "");
//		}
//		else if ( value instanceof SubmodelElementValue smev ) {
//			return AASUtils.writeJson(smev.toJsonObject());
//		}
//		else {
//			return AASUtils.writeJson(value);
//		}
//	}
//	
//	public default void set(Object value) {
//		if ( value instanceof SubmodelElementValue smev ) {
//			setSubmodelElementValue(smev);
//		}
//		else if ( value instanceof SubmodelElement sme ) {
//			setSubmodelElement(sme);
//		}
//		else if ( value instanceof String str ) {
//			str = str.trim();
//			if ( str.startsWith("{") ) {
//				PropertyValue<?> smev = AASUtils.readJson(str, PropertyValue.class);
//				setSubmodelElementValue(smev);
//			}
//			else {
//				PropertyValue<?> propv = (PropertyValue<?>)get();
//				propv.setString(str);
//				setSubmodelElementValue(propv);
//			}
//		}
//		else if ( Primitives.isWrapperType(value.getClass()) ) {
//			setSubmodelElementValue(PropertyValues.fromValue(value));
//		}
//		else {
//			throw new IllegalArgumentException("Unexpected Value: " + value);
//		}
//	}
//	
//	public default void setJsonString(String jsonString) {
//		if ( isValuePort() ) {
//			jsonString = jsonString.trim();
//			if ( jsonString.startsWith("{") ) {
//				PropertyValue<?> smev = AASUtils.readJson(jsonString, PropertyValue.class);
//				setSubmodelElementValue(smev);
//			}
//			else {
//				setSubmodelElementValue(new StringValue(jsonString));
//			}
//		}
//		else {
//			SubmodelElement sme = AASUtils.readJson(jsonString, SubmodelElement.class);
//			setSubmodelElement(sme);
//		}
//	}
//	
//	
//	
//	
//	
////	public SubmodelElementReference getReference() {
////		return m_ref;
////	}
//	
//	
////	public void set(SubmodelElement sme) {
////		Preconditions.checkState(!isInputPort());
////		
////		if ( isValuePort() ) {
////			m_ref.set(ElementValues.getValue(sme));
////		}
////		else {
////			m_ref.set(sme);
////		}
////	}
////	
////	public void set(SubmodelElementValue value) {
////		Preconditions.checkState(!isInputPort());
////		
////		m_ref.set(value);
////	}
//	
////	@Override
////	public String toString() {
////		return String.format("[%s(%s)] %s", m_name, m_type.getTypeString(), m_ref.toString());
////	}
////	
////	@Override
////	public boolean equals(Object obj) {
////		if ( this == obj ) {
////			return true;
////		}
////		else if ( obj == null || obj.getClass() != getClass() ) {
////			return false;
////		}
////		
////		Port other = (Port)obj;
////		return Objects.equals(m_name, other.m_name);
////	}
////	
////	@Override
////	public int hashCode() {
////		return Objects.hash(m_name);
////	}
////	
////	private static Tuple<PortType, String> parseArgName(String argName) {
////		String[] parts = argName.split("\\.");
////		
////		PortType ptype = Funcs.findFirst(Arrays.asList(PortType.values()),
////										pt -> pt.getTypeString().equalsIgnoreCase(parts[0]))
////								.getOrThrow(() -> new IllegalArgumentException("Invalid argument name: " + argName));
////		return Tuple.of(ptype, argName.substring(parts[0].length() + 1));
////	}
}