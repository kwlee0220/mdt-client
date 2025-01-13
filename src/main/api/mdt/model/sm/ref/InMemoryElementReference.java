package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mdt.model.MDTModelSerDe;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class InMemoryElementReference extends AbstractElementReference implements ElementReference {
	private volatile SubmodelElement m_value;
	
	public static InMemoryElementReference of(SubmodelElement init) {
		return new InMemoryElementReference(init);
	}
	
	private InMemoryElementReference(SubmodelElement init) {
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
	
	/**
	 * 주어진 SubmodelElement 값으로 변수을 갱신한다.
	 *
	 * @param newElm	갱신할 새 값.
	 */
	@Override
	public void write(SubmodelElement newElm) {
		m_value = newElm;
	}
	
	public static InMemoryElementReference parseJson(ObjectNode topNode) throws IOException {
		SubmodelElement value = MDTModelSerDe.readValue(topNode, SubmodelElement.class);
		return InMemoryElementReference.of(value);
	}
	
	public static InMemoryElementReference parseString(String refExpr) throws IOException {
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
		
		return InMemoryElementReference.of(value);
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeObject(m_value);
	}
}
