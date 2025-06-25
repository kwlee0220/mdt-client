package mdt.model.sm.variable;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractVariable implements Variable {
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "description";
	
	private final String m_name;
	private final String m_description;
	
	protected AbstractVariable(String name, String description) {
		m_name = name;
		m_description = description;
	}
	
	@JsonProperty("name")
	public String getName() {
		return m_name;
	}
	
	@JsonProperty("description")
	public String getDescription() {
		return m_description;
	}

	public void updateWithValueJsonString(String valueJsonString) throws IOException {
		updateWithValueJsonNode(MDTModelSerDe.readJsonNode(valueJsonString));
	}

	public void updateWithRawString(String externStr) throws IOException {
		externStr = externStr.trim();
		JsonNode rawValue = ( externStr.startsWith("{") )
							? MDTModelSerDe.readJsonNode(externStr)
							: new TextNode(externStr);
		updateWithValueJsonNode(rawValue);
	}
	
	public JsonNode toJsonNode() throws IOException {
		return MDTModelSerDe.toJsonNode(this);
	}
	
	public String toJsonString() throws IOException {
		return MDTModelSerDe.toJsonString(this);
	}

	public static final class ReferenceVariable extends AbstractVariable {
		private static final String FIELD_REFERENCE = "reference";
		public static final String SERIALIZATION_TYPE = "mdt:variable:reference";
		
		private final ElementReference m_ref;

		public ReferenceVariable(String name, String description, ElementReference ref) {
			super(name, description);
			
			m_ref = ref;
		}
		
		public ElementReference getReference() {
			return m_ref;
		}
		
		public void activate(MDTInstanceManager manager) {
			ElementReferences.activate(m_ref, manager);
		}

		@Override
		public SubmodelElement read() throws IOException {
			return m_ref.read();
		}

		@Override
		public ElementValue readValue() throws IOException {
			return m_ref.readValue();
		}

		@Override
		public void update(SubmodelElement sme) throws IOException {
			m_ref.update(sme);
		}

		@Override
		public void updateValue(ElementValue value) throws IOException {
			m_ref.updateValue(value);
		}

		@Override
		public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
			m_ref.updateWithValueJsonNode(valueNode);
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", getName(), m_ref);
		}
		
		public static ReferenceVariable deserializeFields(JsonNode jnode) throws IOException {
			String name = JacksonUtils.getStringField(jnode, FIELD_NAME);
			String description = JacksonUtils.getStringFieldOrNull(jnode, FIELD_DESCRIPTION);
			ElementReference ref = MDTModelSerDe.readValue(jnode.get(FIELD_REFERENCE), ElementReference.class);
			return new ReferenceVariable(name, description, ref);
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeStringField(FIELD_NAME, getName());
			gen.writeStringField(FIELD_DESCRIPTION, getDescription());
			gen.writeObjectField(FIELD_REFERENCE, m_ref);
		}
	}

	public static final class ElementVariable extends AbstractVariable {
		private static final String FIELD_ELEMENT = "element";
		public static final String SERIALIZATION_TYPE = "mdt:variable:element";
		
		private SubmodelElement m_element;

		public ElementVariable(String name, String description, SubmodelElement element) {
			super(name, description);
			
			m_element = element;
		}

		@Override
		public SubmodelElement read() {
			return m_element;
		}

		@Override
		public ElementValue readValue() throws IOException {
			return ElementValues.getValue(m_element);
		}

		@Override
		public void update(SubmodelElement sme) throws IOException {
			m_element = sme;
		}

		@Override
		public void updateValue(ElementValue value) throws IOException {
			ElementValues.update(m_element, value);
		}

		@Override
		public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
			ElementValues.update(m_element, valueNode);
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", getName(), m_element);
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static ElementVariable deserializeFields(JsonNode jnode) throws IOException {
			String name = JacksonUtils.getStringField(jnode, FIELD_NAME);
			String description = JacksonUtils.getStringFieldOrNull(jnode, FIELD_DESCRIPTION);
			SubmodelElement element = MDTModelSerDe.readValue(jnode.get(FIELD_ELEMENT),
																SubmodelElement.class);
			return new ElementVariable(name, description, element);
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeObjectField(FIELD_NAME, getName());
			gen.writeObjectField(FIELD_DESCRIPTION, getDescription());
			gen.writeFieldName(FIELD_ELEMENT);
			gen.writeTree(MDTModelSerDe.toJsonNode(m_element));
		}
	}

	public static final class ValueVariable extends AbstractVariable {
		private static final String FIELD_VALUE = "value";
		private static final String FIELD_VALUE_TYPE = "valueType";
		public static final String SERIALIZATION_TYPE = "mdt:variable:value";
		
		private ElementValue m_value;

		public ValueVariable(String name, String description, ElementValue value) {
			super(name, description);
			
			m_value = value;
		}
		
		@Override
		public SubmodelElement read() {
			throw new UnsupportedOperationException("ValueVariable cannot be read");
		}

		@Override
		public ElementValue readValue() {
			return m_value;
		}

		@Override
		public void update(SubmodelElement sme) throws IOException {
			throw new UnsupportedOperationException("ValueVariable cannot be update(SubmodelElement)");
		}

		@Override
		public void updateValue(ElementValue value) throws IOException {
			m_value = value;
		}

		@Override
		public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
			throw new UnsupportedOperationException("ValueVariable cannot be updateValue(ElementValue)");
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", getName(), m_value);
		}
		
		public static ValueVariable deserializeFields(JsonNode jnode) throws IOException {
			String name = JacksonUtils.getStringField(jnode, FIELD_NAME);
			String description = JacksonUtils.getStringFieldOrNull(jnode, FIELD_DESCRIPTION);
			
			JsonNode valueNode = JacksonUtils.getFieldOrNull(jnode, FIELD_VALUE);
			ElementValue value = ElementValues.parseJsonNode(valueNode);
			return new ValueVariable(name, description, value);
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
			gen.writeStringField(FIELD_NAME, getName());
			gen.writeStringField(FIELD_DESCRIPTION, getDescription());
			gen.writeObjectField(FIELD_VALUE, m_value);
		}
	}
}
