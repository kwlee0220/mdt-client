package mdt.workflow.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import utils.func.FOption;

import mdt.model.workflow.BoolOption;
import mdt.model.workflow.MultilineOption;
import mdt.model.workflow.SMERefOption;
import mdt.model.workflow.StringArrayOption;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.SubmodelRefOption;
import mdt.model.workflow.TwinRefOption;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@SuppressWarnings("serial")
public class OptionDeserializer extends StdDeserializer<Option> {
	public OptionDeserializer() {
		this(null);
	}
	public OptionDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Option deserialize(JsonParser parser, DeserializationContext ctxt)
		throws IOException, JacksonException {
		JsonNode node = parser.getCodec().readTree(parser);

		String name = node.get("name").asText();
		String valType = FOption.mapOrElse(node.get("optionType"), JsonNode::asText,
											OptionValueType.string.name());
		OptionValueType type = OptionValueType.valueOf(valType);
		return switch ( type ) {
			case string -> StringOption.parseJson(name, node);
			case bool -> BoolOption.parseJson(name, node);
			case array -> StringArrayOption.parseJson(name, node);
			case twin_ref -> TwinRefOption.parseJson(name, node);
			case submodel_ref -> SubmodelRefOption.parseJson(name, node);
			case sme_ref -> SMERefOption.parseJson(name, node);
			case multiline -> MultilineOption.parseJson(name, node);
			default -> throw new IllegalStateException("Unexpected OptionValueType: " + type);
		};
	}

}
