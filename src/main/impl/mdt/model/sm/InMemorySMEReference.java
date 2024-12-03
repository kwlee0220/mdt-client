package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InMemorySMEReference extends AbstractSubmodelElementReference implements SubmodelElementReference {
	private volatile SubmodelElement m_value;
	
	public static InMemorySMEReference of(SubmodelElement init) {
		return new InMemorySMEReference(init);
	}
	
	protected InMemorySMEReference(SubmodelElement init) {
		m_value = init;
	}
	
	/**
	 * Variable에서 {@link SubmodelElement}을 읽는다.
	 * 
	 * @return	{@link SubmodelElement} 객체.
	 */
	@Override
	public SubmodelElement read() {
		return m_value;
	}
	
	@Override
	public SubmodelElementValue readValue() {
		return FOption.mapOrElse(m_value, ElementValues::getValue, null);
	}
	
	/**
	 * 주어진 SubmodelElement 값으로 변수을 갱신한다.
	 *
	 * @param newElm	갱신할 새 값.
	 */
	@Override
	public void write(SubmodelElement newElm) {
		m_value = newElm;
	}

	@Override
	public void update(SubmodelElement sme) {
		update(ElementValues.getValue(sme));
	}

	@Override
	public void update(SubmodelElementValue smev) {
		SubmodelElement sme = read();
		if ( sme != null ) {
			write(ElementValues.update(sme, smev));
		}
	}

	@Override
	public String toExternalString() {
		try {
			return super.toExternalString();
		}
		catch ( IOException e ) {
			throw new AssertionError();
		}
	}
	
	public static InMemorySMEReference parseJson(ObjectNode topNode) throws IOException {
		SubmodelElement value = MDTModelSerDe.readValue(topNode, SubmodelElement.class);
		return InMemorySMEReference.of(value);
	}
	
	public static InMemorySMEReference parseString(String refExpr) throws IOException {
		SubmodelElement value;
		if ( !refExpr.trim().startsWith("{") ) {
			value = new DefaultProperty.Builder()
										.value(refExpr)
										.valueType(DataTypeDefXsd.STRING)
										.build();
		}
		else {
			value = MDTModelSerDe.readValue(refExpr, SubmodelElement.class);
		}
		
		return InMemorySMEReference.of(value);
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeObject(m_value);
	}
}
