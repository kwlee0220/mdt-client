package mdt.model.workflow.descriptor.port;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;

import utils.func.FOption;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class PortDescriptorDeserializer extends StdDeserializer<PortDescriptor> {
	private static final long serialVersionUID = 1L;
	
	public PortDescriptorDeserializer() {
		this(null);
	}
	public PortDescriptorDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public PortDescriptor deserialize(JsonParser parser, DeserializationContext ctxt)
		throws IOException, JacksonException {
		ObjectNode node = (ObjectNode)parser.getCodec().readTree(parser);
		
		String name = node.get("name").asText();
		String desc = FOption.map(node.get("description"), JsonNode::asText);
		
		JsonNode refNode = node.get("valueReference");
		if ( refNode != null ) {
			Preconditions.checkState(refNode instanceof TextNode);
			return PortDescriptors.parseStringExpr(name, desc, refNode.asText());
		}
		else {
			boolean valueOnly = FOption.mapOrElse(node.get("valueOnly"), v -> Boolean.parseBoolean(v.asText()), false);
			String portType = FOption.mapOrElse(node.get("portType"), JsonNode::asText,
												PortType.SME.getId()); 
			
			PortType type = PortType.fromId(portType);
			return switch ( type ) {
				case SME -> SubmodelElementPortDescriptor.parseJson(name, desc, valueOnly, node);
				case PARAMETER -> MDTParameterPortDescriptor.parseJson(name, desc, valueOnly, node);
				case FILE -> FilePortDescriptor.parseJson(name, desc, valueOnly, node);
				case LITERAL -> LiteralPortDescriptor.parseJson(name, desc, valueOnly, node);
				case STDOUT -> StdoutPortDescriptor.parseJson(name, desc, valueOnly, node);
				default -> throw new IllegalArgumentException("Unknown PortType: " + type);
			};
		}
	}
}
