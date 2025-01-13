package mdt.model.instance;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ParameterInfoUtils {
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<ParameterInfo> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public ParameterInfo deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			
			String id = node.get("id").asText();
			String type = node.get("type").asText();
			if ( type.startsWith("xs:") ) {
				String value = getNullableString(node, "value");
				return new PropertyParameterInfo(id, type, value);
			}
			else if ( type.equals("File") ) {
				String value = getNullableString(node, "value");
				String contentType = getNullableString(node, "contentType");
				if ( contentType == null ) {
					throw new IOException(String.format("Null contentType: json=%s", MDTModelSerDe.toJsonString(node)));
				}
				return new FileParameterInfo(id, contentType, value);
			}
			else {
				throw new IOException("unknown ParameterInfo: type=" + type);
			}
		}
	}
	
	private static String getNullableString(JsonNode node, String name) {
		JsonNode child = node.get(name);
		return (child != null && !child.isNull()) ? child.asText() : null;
	}

	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ParameterInfo> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<ParameterInfo> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(ParameterInfo ref, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			ref.serialize(gen);
		}
	}
}
