package mdt.model.sm.value;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import mdt.model.sm.value.ElementValues.Deserializer;
import mdt.model.sm.value.ElementValues.Serializer;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = Serializer.class)
@JsonDeserialize(using = Deserializer.class)
public class TypedElementValue {
	public static enum Type {
		PROPERTY(PropertyValue.class),
		FILE(FileValue.class),
		MULTI_LANGUAGE_PROPERTY(MultiLanguagePropertyValue.class),
		RANGE(RangeValue.class),
		REFERENCE(ReferenceElementValue.class),
		COLLECTION(ElementCollectionValue.class),
		LIST(ElementListValue.class);
		
		private final Class<? extends ElementValue> m_valueClass;
		
		private Type(Class<? extends ElementValue> valueClass) {
			m_valueClass = valueClass;
		}
		
		public Class<?> getValueClass() {
			return m_valueClass;
		}
	};
	
	private final Type m_type;
	private final ElementValue m_value;
	
	private TypedElementValue(Type type, ElementValue value) {
		m_type = type;
		m_value = value;
	}
	
	public Type getType() {
		return m_type;
	}
	
	public ElementValue getValue() {
		return m_value;
	}
	
	public static TypedElementValue from(ElementValue value) {
		if ( value instanceof PropertyValue ) {
			return new TypedElementValue(Type.PROPERTY, value);
		}
		else if ( value instanceof FileValue ) {
			return new TypedElementValue(Type.FILE, value);
		}
		else if ( value instanceof MultiLanguagePropertyValue ) {
			return new TypedElementValue(Type.MULTI_LANGUAGE_PROPERTY, value);
		}
		else if ( value instanceof ElementCollectionValue ) {
			return new TypedElementValue(Type.COLLECTION, value);
		}
		else if ( value instanceof ElementListValue ) {
			return new TypedElementValue(Type.LIST, value);
		}
		else if ( value instanceof RangeValue ) {
			return new TypedElementValue(Type.RANGE, value);
		}
		else if ( value instanceof ReferenceElementValue ) {
			return new TypedElementValue(Type.REFERENCE, value);
		}
		else {
			throw new IllegalArgumentException("unknown type: " + value.getClass());
		}
	}
	
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<TypedElementValue> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<TypedElementValue> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(TypedElementValue tev, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField("elementValueType", tev.getType().name());
			gen.writeObjectField("value", tev.getValue());
			gen.writeEndObject();
		}
	}

	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<TypedElementValue> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public TypedElementValue deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			
			String typeName = jnode.get("elementValueType").asText();
			Type type = Type.valueOf(typeName.toUpperCase());
			ElementValue smev = parser.getCodec().treeToValue(jnode.get("value"), type.m_valueClass);
			
			return new TypedElementValue(type, smev);
		}
	}
}
