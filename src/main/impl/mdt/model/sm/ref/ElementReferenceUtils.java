package mdt.model.sm.ref;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;

import utils.Utilities;
import utils.func.FOption;
import utils.func.Tuple;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementReferenceUtils {
	public static ElementReference parseJsonString(String jsonStr) throws IOException {
        return MDTModelSerDe.readValue(jsonStr, ElementReference.class);
    }
	
	public static ElementReference parseJsonNode(JsonNode node) throws IOException {
        return MDTModelSerDe.readValue(node, ElementReference.class);
    }
	
	/**
	 * 주어진 문자열 표현을 파싱하여 {@link ElementReference} 객체로 변환한다.
	 * 
	 * @param expr	문자열 표현
	 * @return	변환된 {@link ElementReference} 객체
	 * @throws IOException	문자열 표현에 오류가 있는 경우.
	 */
	public static ElementReference parseString(String expr) throws IOException {
		// 'stdout' 특별 처리
		// 일반적인 형태로 표현되지 않기 때문.
		if ( expr.equalsIgnoreCase(ElementReferenceType.STDOUT.getCode()) ) {
			return new StdoutElementReference();
		}
		
		// ElementReference 문자열은 다음과 같은 형태로 표현된다.
		//   <referenceType>:<referenceString>
		
		// referenceType을 추출한다. 만일 ':' 문자가 없는 경우에는 'default'로 간주한다.
		Tuple<String,String> parts = Utilities.split(expr, ':', Tuple.of("default", expr));	
		ElementReferenceType refType = ElementReferenceType.fromName(parts._1);
		
		String refStrExpr = parts._2;
		return switch ( refType ) {
			// 문자열 형태: [default:]<submodelElementPath>
			case DEFAULT -> DefaultElementReference.parseString(refStrExpr);
			// 문자열 형태: parameter:<instanceId>:<parameterId>
			case PARAMETER -> MDTParameterReference.parseString(refStrExpr);
			// 문자열 형태: file:<file-path>
			case FILE -> FileStoreElementReference.parseString(refStrExpr);
			// 문자열 형태: opvar:<instanceId>/<submodelIdShort>/<opIdShortPath>/<kind>/<ordinal>
			case OPERATION_VARIABLE -> OperationVariableReference.parseString(refStrExpr);
			// 문자열 형태: argument:<operationId>:<argumentId>
			case ARGUMENT -> MDTArgumentReference.parseString(refStrExpr);
			// 문자열 형태: memory:<value|value_json>
			case IN_MEMORY -> InMemoryElementReference.parseString(refStrExpr);
			// 문자열 형태: literal:<literalValue>
			case LITERAL -> LiteralElementReference.parseString(refStrExpr);
			// Invalid SubmodelElementReference expression
			default -> throw new IllegalArgumentException("Invalid SubmodelElementReference expression: " + expr);
		};
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
			JsonNode node = parser.getCodec().readTree(parser);
			if ( node instanceof TextNode ) {
				return ElementReferenceUtils.parseString(node.asText());
			}
			
			Preconditions.checkState(node instanceof ObjectNode);
			ObjectNode root = (ObjectNode)node;
			
			String refType = FOption.mapOrElse(node.get(ElementReference.FIELD_REFERENCE_TYPE), JsonNode::asText,
												ElementReferenceType.DEFAULT.name());
			
			ElementReferenceType type = ElementReferenceType.fromName(refType);
			return switch ( type ) {
				case DEFAULT -> DefaultElementReference.parseJson(root);
				case OPERATION_VARIABLE -> OperationVariableReference.parseJson(root);
				case PARAMETER -> MDTParameterReference.parseJson(root);
				case ARGUMENT -> MDTArgumentReference.parseJson(root);
				case FILE -> FileStoreElementReference.parseJson(root);
				case LITERAL -> LiteralElementReference.parseJson(root);
				case STDOUT -> StdoutElementReference.parseJson(root);
				default -> throw new IllegalArgumentException("Unknown ElementReference: type=" + type);
			};
		}
	}

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
			ref.serialize(gen);
		}
	}
}
