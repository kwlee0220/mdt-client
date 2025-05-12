package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExprParser;
import mdt.model.sm.ref.MDTSubmodelReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTSubmodelRefOption extends AbstractOption<MDTSubmodelReference> {
	public static final String SERIALIZATION_TYPE = "mdt:option:submodel_ref";
	
	public MDTSubmodelRefOption(String name, MDTSubmodelReference ref) {
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
	
	public static MDTSubmodelRefOption parseStringExpr(String name, String expr) {
		MDTSubmodelReference elmRef = MDTExprParser.parseSubmodelReference(expr).evaluate();
		return new MDTSubmodelRefOption(name, elmRef);
	}
	
	public static MDTSubmodelRefOption deserializeFields(JsonNode jnode) throws IOException {
		String name = JacksonUtils.getStringField(jnode, "name");
		MDTSubmodelReference ref = MDTModelSerDe.readValue(jnode.get("value"), MDTSubmodelReference.class);
		return new MDTSubmodelRefOption(name, ref);
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeObjectField("value", getValue());
	}
}
