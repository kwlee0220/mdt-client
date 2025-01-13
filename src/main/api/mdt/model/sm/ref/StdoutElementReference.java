package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.PropertyUtils;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class StdoutElementReference extends AbstractElementReference implements ElementReference {
	public SubmodelElement read() throws IOException {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not provide 'read()'");	
	}

	@Override
	public void write(SubmodelElement sme) {
		String str = MDTModelSerDe.toJsonString(sme);
		System.out.println(str);
	}
	
	@Override
	public SubmodelElement update(SubmodelElementValue smev) {
		String str = MDTModelSerDe.toJsonString(smev);	
		System.out.println(str);
		
		return PropertyUtils.STRING("stdout", str);
	}

	@Override
	public SubmodelElement updateWithValueJsonNode(JsonNode valueNode) throws IOException {
		String str = MDTModelSerDe.toJsonString(valueNode);	
		System.out.println(str);
		
		return PropertyUtils.STRING("stdout", str);
	}
	
	@Override
	public String toString() {
		return "stdout";
	}
	
	public static StdoutElementReference parseJson(ObjectNode topNode) {
		return new StdoutElementReference();
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(ElementReference.FIELD_REFERENCE_TYPE, ElementReferenceType.STDOUT.getCode());
		gen.writeEndObject();
	}
	
	public static final void main(String... args) throws Exception {
		StdoutElementReference ref = new StdoutElementReference();
		System.out.println(ref);
		ref.write(PropertyUtils.STRING("name", "XXX"));
	}
}
