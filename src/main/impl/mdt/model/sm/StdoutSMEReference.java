package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class StdoutSMEReference extends AbstractSubmodelElementReference implements SubmodelElementReference {
	public SubmodelElement read() throws IOException {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not provide 'read()'");	
	}

	@Override
	public void write(SubmodelElement sme) {
		String str = MDTModelSerDe.toJsonString(sme);
		System.out.println(str);
	}
	
	@Override
	public void update(SubmodelElementValue smev) {
		String str = MDTModelSerDe.toJsonString(smev);	
		System.out.println(str);
	}

	@Override
	public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
		String str = MDTModelSerDe.toJsonString(valueNode);	
		System.out.println(str);
	}
	
	@Override
	public String toString() {
		return "stdout";
	}
	
	public static StdoutSMEReference parseJson(ObjectNode topNode) {
		return new StdoutSMEReference();
	}
}
