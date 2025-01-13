package mdt.model.sm.ref;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class LiteralElementReference extends AbstractElementReference implements ElementReference {
	private final String m_literal;
	private final DataTypeDefXsd m_valueType;
	
	public LiteralElementReference(DataTypeDefXsd valueType, String literal) {
		Preconditions.checkArgument(valueType != null);
		
		m_literal = literal;
		m_valueType = valueType;
	}
	
	public String getLiteral() {
		return m_literal;
	}
	
	public DataTypeDefXsd getValueType() {
		return m_valueType;
	}
	
	public SubmodelElement read() throws IOException {
		return new DefaultProperty.Builder()
									.valueType(m_valueType)
									.value(m_literal)
									.build();
	}
	
	public void write(SubmodelElement sme) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not provide 'write()'");		
	}
	
	@Override
	public String toString() {
		return String.format("literal:%s (%s)", m_literal, m_valueType);
	}

	private static Pattern compiledPattern = Pattern.compile("^(.*?)\\s*\\((.*?)\\)$");
	public static LiteralElementReference parseString(String valueExpr) {
        Matcher matcher = compiledPattern.matcher(valueExpr);
		if ( matcher.matches() ) {
			String literal = matcher.group(1);
			String valueType = matcher.group(2);
			
			return new LiteralElementReference(DataTypes.fromDataTypeName(valueType).getTypeDefXsd(), literal);
		}
		else {
			throw new IllegalArgumentException("invalid LiteralElementReference string: " + valueExpr);
		}
	}
	
	public static LiteralElementReference parseJson(ObjectNode topNode) {
		String literal = topNode.get("literal").asText();
		DataTypeDefXsd dtype = DataTypes.fromDataTypeName(topNode.get("valueType").asText()).getTypeDefXsd();
		
		return new LiteralElementReference(dtype, literal);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField(ElementReference.FIELD_REFERENCE_TYPE, ElementReferenceType.LITERAL.getCode());
		gen.writeStringField("literal", m_literal);
		gen.writeStringField("valueType", m_valueType.name());
		gen.writeEndObject();
	}
	
	public static final void main(String... args) throws Exception {
		LiteralElementReference ref = new LiteralElementReference(DataTypeDefXsd.STRING, "hello");
		System.out.println(ref);
		
		ref = LiteralElementReference.parseString("world (string)");
		System.out.println(ref);
		
		ref = LiteralElementReference.parseString("-1.1 (float)");
		System.out.println(ref);
		String json = MDTModelSerDe.toJsonString(ref);
		System.out.println(json);
		
		ObjectNode node = (ObjectNode)MDTModelSerDe.toJsonNode(ref);
		node.put("literal", 75.3);
		System.out.println(MDTModelSerDe.toJsonString(node));
		ref = MDTModelSerDe.readValue(node, LiteralElementReference.class);
		System.out.println(ref.readAsFloat());
		
		ref = LiteralElementReference.parseString("PT1s (duration)");
		System.out.println(ref);
	}
}
