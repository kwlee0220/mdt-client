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

import utils.Preconditions;
import utils.SplitStream;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.ArgumentReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.DefaultElementReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.OperationVariableReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.ParameterReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.TimeseriesReferenceExpr;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.expr.MDTParserException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.timeseries.ReadLastRecordsReference;
import mdt.model.sm.ref.timeseries.TimeSeriesElementReference;


/**
 * {@link ElementReference}에 대한 정적 유틸리티 모음이다.
 * <p>
 * 참조 활성화({@link #activate(ElementReference, MDTInstanceManager)}), Json 직렬화/역직렬화
 * ({@link #parseJsonString(String)}, {@link #parseJsonNode(JsonNode)}, {@link Serializer},
 * {@link Deserializer}), 표현식 문자열 파싱({@link #parseExpr(String)})을 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementReferences {
	/**
	 * 주어진 {@link ElementReference}를 활성화한다.
	 *
	 * @param ref		활성화할 참조. {@link MDTElementReference}이어야 한다.
	 * @param manager	활성화에 사용할 {@link MDTInstanceManager}.
	 * @throws IllegalArgumentException	{@code ref}가 {@link MDTElementReference}가 아닌 경우.
	 */
	public static void activate(ElementReference ref, MDTInstanceManager manager) {
		Preconditions.checkNotNullArgument(ref, "ElementReference is null");
		Preconditions.checkNotNullArgument(manager, "MDTInstanceManager is null");

		if ( ref instanceof MDTElementReference mdtRef ) {
			mdtRef.activate(manager);
		}
		else {
			throw new IllegalArgumentException("ElementReference is not an MDTElementReference: " + ref);
		}
	}
	
	/**
	 * Json 문자열로부터 {@link ElementReference}를 복원한다.
	 *
	 * @param jsonStr	{@code @type} 필드를 포함한 Json 문자열.
	 * @return 복원된 {@link ElementReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static ElementReference parseJsonString(String jsonStr) throws IOException {
        return MDTModelSerDe.readValue(jsonStr, ElementReference.class);
    }

	/**
	 * Json 노드로부터 {@link ElementReference}를 복원한다.
	 *
	 * @param node	{@code @type} 필드를 포함한 Json 노드.
	 * @return 복원된 {@link ElementReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static ElementReference parseJsonNode(JsonNode node) throws IOException {
        return MDTModelSerDe.readValue(node, ElementReference.class);
    }

	/**
	 * 표현식 문자열로부터 {@link DefaultSubmodelReference}를 파싱한다.
	 * <p>
	 * 형식: {@code <instanceId>:<submodelIdShort>} 또는 {@code submodel:<submodelId>}.
	 *
	 * @param exprStr	Submodel 참조 표현식.
	 * @return 파싱된 {@link DefaultSubmodelReference} 객체.
	 * @throws MDTParserException	표현식이 올바르지 않은 경우.
	 */
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
	
	/**
	 * 표현식 문자열로부터 {@link MDTElementReference}를 파싱한다.
	 * <p>
	 * 선두 라벨에 따라 참조 종류가 결정된다.
	 * <ul>
	 *   <li>{@code param:<instanceId>:<paramSpec>} → {@link MDTParameterReference}
	 *       (단, {@code paramSpec}이 {@code "*"}이면 {@link MDTParameterCollectionReference})</li>
	 *   <li>{@code oparg:<instanceId>:<submodelIdShort>:<kind>:<argSpec>} → {@link MDTArgumentReference}</li>
	 *   <li>{@code opvar:<instanceId>:<submodelIdShort>:<kind>:<ordinal>} → {@link OperationVariableReference}</li>
	 *   <li>{@code timeseries:<submodelSpec>:<idShortPath>[#<range>][|<columns>]} → {@link TimeSeriesElementReference}
	 *       (range: {@code last=N}(마지막 N개) | {@code last=dur[@now|@latest]} | {@code from~to})</li>
	 *   <li>그 외(라벨 없음) {@code <instanceId>:<submodelIdShort>:<elementPath>} → {@link DefaultElementReference}</li>
	 * </ul>
	 *
	 * @param refStr	ElementReference 표현식.
	 * @return 파싱된 {@link MDTElementReference} 객체.
	 * @throws MDTParserException	표현식이 올바르지 않은 경우.
	 */
	public static MDTElementReference parseExpr(String refStr) {
		Preconditions.checkNotNullArgument(refStr, "ElementReference expression is null");
		
		MDTElementReferenceExpr expr = MDTExpressionParser.parseElementReference(refStr);
		if ( expr instanceof ParameterReferenceExpr paramExpr ) {
			return paramExpr.evaluate();
		}
		else if ( expr instanceof DefaultElementReferenceExpr defaultExpr ) {
			return defaultExpr.evaluate();
		}
		else if ( expr instanceof ArgumentReferenceExpr argExpr ) {
			return argExpr.evaluate();
		}
		else if ( expr instanceof OperationVariableReferenceExpr opvExpr ) {
			return opvExpr.evaluate();
		}
		else if ( expr instanceof TimeseriesReferenceExpr tsExpr ) {
			return tsExpr.evaluate();
		}
		else {
			throw new MDTParserException("Unsupported MDTElementReference expression: expr=" + refStr);
		}
	}
	
	private static final String FIELD_TYPE = "@type";
	/**
	 * {@link ElementReference}를 {@code @type} 필드와 함께 Json으로 직렬화하는 Jackson serializer.
	 */
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

	/**
	 * {@code @type} 필드를 보고 해당하는 {@link ElementReference} 구현체로 역직렬화하는 Jackson deserializer.
	 */
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
	
	/**
	 * {@code @type} 필드를 가진 Json 노드로부터 해당 타입의 {@link ElementReference}를 복원한다.
	 *
	 * @param jnode	{@code @type} 필드를 포함한 Json 노드.
	 * @return 복원된 {@link ElementReference} 객체.
	 * @throws IOException	{@code @type}이 없거나 미등록 타입이거나, 해석 과정에서 예외가 발생한 경우.
	 */
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
				JsonNode paramExpr = SubmodelBasedElementReference.checkJsonField(jnode,
																MDTParameterReference.FIELD_PARAMETER_EXPR);
				if ( paramExpr.asText().equals("*") ) {
					return MDTParameterCollectionReference.deserializeFields(jnode);
				}
				else {
					return MDTParameterReference.deserializeFields(jnode);
				}
			case MDTArgumentReference.SERIALIZATION_TYPE:
				return MDTArgumentReference.deserializeFields(jnode);
			case OperationVariableReference.SERIALIZATION_TYPE:
				return OperationVariableReference.deserializeFields(jnode);
			case FileStoreReference.SERIALIZATION_TYPE:
				return FileStoreReference.deserializeFields(jnode);
			case ReadLastRecordsReference.SERIALIZATION_TYPE:
				return ReadLastRecordsReference.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered Option type: " + type);
		}
	}
}
