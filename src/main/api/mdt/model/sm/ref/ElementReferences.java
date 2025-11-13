package mdt.model.sm.ref;

import java.io.IOException;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;

import utils.SplitStream;
import utils.func.Try;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTParserException;
import mdt.model.instance.MDTInstanceManager;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementReferences {
	public static void activate(ElementReference ref, MDTInstanceManager manager) {
		Preconditions.checkArgument(ref != null, "ElementReference is null");
		Preconditions.checkArgument(manager != null, "MDTInstanceManager is null");

		if ( ref instanceof MDTElementReference mdtRef ) {
			mdtRef.activate(manager);
		}
		else {
			throw new IllegalArgumentException("ElementReference is not an MDTElementReference: " + ref);
		}
	}
	
	public static ElementReference parseJsonString(String jsonStr) throws IOException {
        return MDTModelSerDe.readValue(jsonStr, ElementReference.class);
    }
	
	public static ElementReference parseJsonNode(JsonNode node) throws IOException {
        return MDTModelSerDe.readValue(node, ElementReference.class);
    }

	public static DefaultSubmodelReference parseSubmodelReference(String exprStr) {
		SplitStream tokens = SplitStream.of(exprStr, ':');
		DefaultSubmodelReference ref = parseSubmodelReference(tokens);
		if ( tokens.hasNext() ) {
			throw new MDTParserException("Invalid submodel reference: expr=" + exprStr);
		}
		
		return ref;
	}
	private static DefaultSubmodelReference parseSubmodelReference(SplitStream tokens) {
		String fullExpr = tokens.remaining();
		Supplier<IllegalArgumentException> errorMsg
							= () -> new IllegalArgumentException("Invalid DefaultSubmodelReference: expr=" + fullExpr);
		
		String first = tokens.nextToken().getOrThrow(errorMsg);
		String second = tokens.nextToken().getOrThrow(errorMsg);
		switch ( first ) {
			case "submodel":
				return DefaultSubmodelReference.ofId(second);
			default:
				return DefaultSubmodelReference.ofIdShort(first, second);
		}
	}
	
	public static MDTElementReference parseExpr(String exprStr) {
		Preconditions.checkArgument(exprStr != null, "ElementReference expression is null");
		
		SplitStream tokens = SplitStream.of(exprStr, ':');
		MDTElementReference ref = parseExpr(tokens);
		if ( tokens.hasNext() ) {
			throw new MDTParserException("Invalid ElementReference: expr=" + exprStr);
		}
		
		return ref;
	}

	private static MDTElementReference parseExpr(SplitStream tokens) {
		String label = tokens.next();
		switch ( label.toLowerCase() ) {
			case "param":
				return parseParameterReference(tokens);
			case "oparg":
				return parseArgumentReference(tokens);
			case "opvar":
				return parseOperationVariableReference(tokens);
			default:
				tokens.pushBack(label);
				return parseDefaultElementReference(tokens);
				
		}
	}
	private static DefaultElementReference parseDefaultElementReference(SplitStream tokens) {
		String fullExpr = tokens.remaining();
		Supplier<IllegalArgumentException> errorMsg
						= () -> new IllegalArgumentException("Invalid DefaultElementReference: expr=" + fullExpr);
						
		DefaultSubmodelReference smRef = parseSubmodelReference(tokens);
		String elementPath = tokens.nextToken().getOrThrow(errorMsg);
		return DefaultElementReference.newInstance(smRef, elementPath);
	}
	private static SubmodelBasedElementReference parseParameterReference(SplitStream tokens) {
		String fullExpr = tokens.remaining();
		Supplier<IllegalArgumentException> errorMsg
						= () -> new IllegalArgumentException("Invalid MDTParameterReference: expr=" + fullExpr);
						
		String instanceId = tokens.nextToken().getOrThrow(errorMsg);
		String paramSpec = tokens.nextToken().getOrThrow(errorMsg);
		if ( paramSpec.equals("*") ) {
			return MDTParameterCollectionReference.newInstance(instanceId);
		}
		else {
			return MDTParameterReference.newInstance(instanceId, paramSpec);
		}
	}
	private static MDTArgumentReference parseArgumentReference(SplitStream tokens) {
		String fullExpr = tokens.remaining();
		Supplier<IllegalArgumentException> errorMsg
							= () -> new IllegalArgumentException("Invalid MDTArgumentReference: expr=" + fullExpr);
							
		DefaultSubmodelReference smRef = parseSubmodelReference(tokens);
		MDTArgumentKind inout = MDTArgumentKind.fromString(tokens.nextToken().getOrThrow(errorMsg));
		String spec = tokens.nextToken().getOrThrow(errorMsg);
		return MDTArgumentReference.builder()
									.submodelReference(smRef)
									.kind(inout)
									.argument(spec)
									.build();
	}
	private static OperationVariableReference parseOperationVariableReference(SplitStream tokens) {
		String fullExpr = tokens.remaining();
		Supplier<IllegalArgumentException> errorMsg
							= () -> new IllegalArgumentException("Invalid OperationVariableReference: expr=" + fullExpr);
							
		MDTElementReference opRef = parseExpr(tokens);
		
		OperationVariableReference.Kind kind = OperationVariableReference.Kind.fromString(
																			tokens.nextToken().getOrThrow(errorMsg));
		String ordinalStr = tokens.nextToken().getOrThrow(errorMsg);
		int ordinal = Try.get(() -> Integer.parseInt(ordinalStr)).getOrThrow(errorMsg);
		
		return OperationVariableReference.newInstance(opRef, kind, ordinal);
	}
	
	private static final String FIELD_TYPE = "@type";
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ElementReference> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<ElementReference> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(ElementReference ref, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_TYPE, ref.getSerializationType());
			ref.serializeFields(gen);
			gen.writeEndObject();
		}
	}

	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<ElementReference> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public ElementReference deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseTypedJsonNode(jnode);
		}
	}
	
	public static ElementReference parseTypedJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new JacksonDeserializationException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode));
		}
		
		switch ( type ) {
			case DefaultElementReference.SERIALIZATION_TYPE:
				return DefaultElementReference.deserializeFields(jnode);
			case MDTParameterReference.SERIALIZATION_TYPE:
				return MDTParameterReference.deserializeFields(jnode);
			case MDTArgumentReference.SERIALIZATION_TYPE:
				return MDTArgumentReference.deserializeFields(jnode);
			case OperationVariableReference.SERIALIZATION_TYPE:
				return OperationVariableReference.deserializeFields(jnode);
			case FileStoreReference.SERIALIZATION_TYPE:
				return FileStoreReference.deserializeFields(jnode);
//			case MDTSubmodelRefOption.SERIALIZATION_TYPE:
//				return MDTSubmodelRefOption.deserializeFields(jnode);
//			case MDTInstanceRefOption.SERIALIZATION_TYPE:
//				return MDTInstanceRefOption.deserializeFields(jnode);
//			case MultiLineOption.SERIALIZATION_TYPE:
//				return MultiLineOption.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered Option type: " + type);
		}
	}
}
