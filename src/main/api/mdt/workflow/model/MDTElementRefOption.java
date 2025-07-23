package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTElementRefOption extends AbstractOption<MDTElementReference> {
	public static final String SERIALIZATION_TYPE = "mdt:option:element-ref";
	
	public MDTElementRefOption(String name, MDTElementReference ref) {
		super(name, ref);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of("--" + getName(), getValue().toStringExpr());
	}
	
	public static MDTElementRefOption parseStringExpr(String name, String expr) {
		MDTElementReference elmRef = ElementReferences.parseExpr(expr);
		return new MDTElementRefOption(name, elmRef);
	}
	
	public static MDTElementRefOption deserializeFields(JsonNode jnode) throws IOException {
		String name = JacksonUtils.getStringField(jnode, "name");
		MDTElementReference ref = MDTModelSerDe.readValue(jnode.get("value"), MDTElementReference.class);
		return new MDTElementRefOption(name, ref);
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeObjectField("value", getValue());
	}
}
