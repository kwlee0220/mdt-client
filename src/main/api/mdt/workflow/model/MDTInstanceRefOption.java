package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonUtils;

import mdt.model.expr.MDTExprParser;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class MDTInstanceRefOption extends AbstractOption<String> {
	public static final String SERIALIZATION_TYPE = "mdt:option:instance_ref";
	
	public MDTInstanceRefOption(String name, String ref) {
		super(name, ref);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public List<String> toCommandOptionSpec() {
		return List.of("--" + getName(), "mdt:" + getValue());
	}
	
	public static MDTInstanceRefOption parseStringExpr(String name, String expr) {
		String instRef = MDTExprParser.parseInstanceReference(expr).evaluate();
		return new MDTInstanceRefOption(name, instRef);
	}
	
	public static MDTInstanceRefOption deserializeFields(JsonNode jnode) throws IOException {
		String name = JacksonUtils.getStringField(jnode, "name");
		String ref = jnode.get("value").asText();
		return new MDTInstanceRefOption(name, ref);
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeStringField("name", getName());
		gen.writeStringField("value", getValue());
	}
}
