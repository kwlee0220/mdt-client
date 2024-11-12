package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mdt.aas.DataTypes;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class LiteralSMEReference extends AbstractSubmodelElementReference implements SubmodelElementReference {
	private String m_literal;
	private DataTypeDefXsd m_dataType;
	
	public LiteralSMEReference(String literal, DataTypeDefXsd dataType) {
		m_literal = literal;
		m_dataType = dataType;
	}
	
	public LiteralSMEReference(String literal) {
		this(literal, DataTypeDefXsd.STRING);
	}
	
	public String getLiteral() {
		return m_literal;
	}
	
	public SubmodelElement read() throws IOException {
		return new DefaultProperty.Builder()
									.value(m_literal)
									.valueType(m_dataType)
									.build();
	}
	
	public void write(SubmodelElement sme) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not provide 'write()'");		
	}
	
	@Override
	public String toString() {
		return "literal:" + m_literal;
	}

	public static LiteralSMEReference parseString(String valueExpr) {
		return new LiteralSMEReference(valueExpr);
	}
	
	public static LiteralSMEReference parseJson(ObjectNode topNode) {
		String literal = topNode.get("literal").asText();
		DataTypeDefXsd dtype = DataTypes.fromDataTypeName(topNode.get("dataType").asText()).getTypeDefXsd();
		
		return new LiteralSMEReference(literal, dtype);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField("referenceType", SubmodelElementReferenceType.LITERAL.name().toLowerCase());
		gen.writeStringField("literal", m_literal);
		gen.writeStringField("dataType", m_dataType.name());
		gen.writeEndObject();
	}
}
