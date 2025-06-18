package mdt.workflow.model;

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

import lombok.experimental.UtilityClass;

import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Options {
	private static final String FIELD_TYPE = "@type";
	
	public static StringOption newOption(String name, String value) {
		return new StringOption(name, value);
	}
	
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<Option<?>> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<Option<?>> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(Option<?> serde, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_TYPE, serde.getSerializationType());
			serde.serializeFields(gen);
			gen.writeEndObject();
		}
	}

	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<Option<?>> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public Option<?> deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseTypedJsonNode(jnode);
		}
	}
	
	public static Option<?> parseTypedJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new JacksonDeserializationException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode));
		}
		
		switch ( type ) {
			case StringOption.SERIALIZATION_TYPE:
				return StringOption.deserializeFields(jnode);
			case MDTElementRefOption.SERIALIZATION_TYPE:
				return MDTElementRefOption.deserializeFields(jnode);
			case MDTSubmodelRefOption.SERIALIZATION_TYPE:
				return MDTSubmodelRefOption.deserializeFields(jnode);
			case MDTInstanceRefOption.SERIALIZATION_TYPE:
				return MDTInstanceRefOption.deserializeFields(jnode);
			case MultiLineOption.SERIALIZATION_TYPE:
				return MultiLineOption.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered Option type: " + type);
		}
	}
}
