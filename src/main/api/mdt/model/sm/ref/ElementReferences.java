package mdt.model.sm.ref;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;
import utils.json.JsonNodeStdDeserializer;
import utils.json.MultiTypeJsonNodeDeserializer;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.expr.MDTParserException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.timeseries.TimeSeriesRecordsReference;


/**
 * {@link ElementReference}에 대한 정적 유틸리티 모음이다.
 * <p>
 * 표현식 문자열 파싱({@link #parseExpr(String)}, {@link #parseSubmodelExpr(String)})과 Json
 * 직렬화/역직렬화({@link #parseJsonString(String)}, {@link #parseJsonNode(JsonNode)}, {@link Serializer},
 * {@link Deserializer})를 제공한다.
 * <p>
 * 표현식으로 만든 참조는 모두 {@link MDTElementReference}이므로 {@link #parseExpr(String)}는
 * {@link MDTElementReference}를 돌려준다. 반면 JSON으로만 만들 수 있는 {@link FileStoreReference}는
 * {@link MDTElementReference}가 아니므로 JSON 역직렬화는 {@link ElementReference}를 돌려준다.
 * 참조 활성화는 {@link MDTElementReference#activate(MDTInstanceManager)}를 직접 부른다
 * (2026-11-01에 {@code ElementReferences.activate(ElementReference, MDTInstanceManager)}를 지웠다).
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ElementReferences {
	private ElementReferences() {
		throw new AssertionError("Should not be called: class=" + ElementReferences.class);
	}

	/**
	 * Json 문자열로부터 {@link ElementReference}를 복원한다.
	 *
	 * <p>
	 * 문자열 전체가 JSON 값 하나여야 하며({@link MDTModelSerDe#readSingleJsonNode(String)}), 읽은 노드를
	 * {@link #parseJsonNode(JsonNode)}에 넘기므로 예외와 메시지도 그와 같다. 2026-11-01 이전에는
	 * {@code MDTModelSerDe.readValue}를 거쳐 {@code "null"}이면 {@code null}을 돌려주고, 첫 값 뒤의 입력을
	 * 무시했으며, 오류 메시지에 JSON 전체를 넣었다.
	 *
	 * @param jsonStr	{@code @type} 필드를 포함한 Json 문자열.
	 * @return 복원된 {@link ElementReference} 객체({@code null}이 아님).
	 * @throws IllegalArgumentException	{@code jsonStr}이 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 포함), 미등록 타입이거나,
	 * 											필수 필드가 없거나 타입이 맞지 않는 경우.
	 * @throws IOException	JSON 문법 오류이거나 JSON 값 뒤에 다른 입력이 있는 경우.
	 */
	public static ElementReference parseJsonString(String jsonStr) throws IOException {
		Preconditions.checkNotNullArgument(jsonStr, "JSON string is null");
		return parseJsonNode(MDTModelSerDe.readSingleJsonNode(jsonStr));
	}

	/**
	 * {@code @type} 필드를 가진 Json 노드로부터 해당 타입의 {@link ElementReference}를 복원한다.
	 * <p>
	 * Jackson을 거치지 않고 등록부에 바로 넘기므로, 구현체가 던진 예외가 그대로 전달된다.
	 * <p>
	 * 반환 타입이 {@link MDTElementReference}가 아니라 {@link ElementReference}인 것은 JSON으로만 만들 수 있는
	 * {@link FileStoreReference}가 {@link MDTElementReference}가 아니기 때문이다.
	 *
	 * @param node	{@code @type} 필드를 포함한 Json 노드.
	 * @return 복원된 {@link ElementReference} 객체.
	 * @throws IllegalArgumentException	{@code node}가 {@code null}인 경우.
	 * @throws JacksonDeserializationException	{@code @type}이 없거나(JSON {@code null} 노드 포함),
	 * 										문자열이 아니거나, 미등록 타입인 경우.
	 * @throws IOException	필수 필드가 없거나 타입이 맞지 않는 등 구현체가 노드를 해석하지 못한 경우
	 * 						(필드 오류는 {@link JacksonDeserializationException}).
	 */
	public static ElementReference parseJsonNode(JsonNode node) throws IOException {
		return DESERIALIZER.deserialize(node);
	}

	/**
	 * 표현식 문자열로부터 {@link DefaultSubmodelReference}를 파싱한다.
	 * <p>
	 * 2026-11-01 이전 이름은 {@code parseSubmodelReference}이다(별칭 없이 바꿈). {@link #parseExpr(String)}와
	 * 짝을 맞췄다.
	 * <p>
	 * MdtExpr 문법의 {@code submodelSpec} 규칙을 따른다:
	 * {@code <instanceId>:[idShort=]<submodelIdShort>} 또는 {@code submodel:id=<submodelId>}.
	 *
	 * @param exprStr	Submodel 참조 표현식.
	 * @return 파싱된 {@link DefaultSubmodelReference} 객체.
	 * @throws IllegalArgumentException	{@code exprStr}이 {@code null}인 경우.
	 * @throws MDTParserException	표현식의 문법이 틀렸거나 뒤에 남는 입력이 있는 경우(2026-11-01 이전 문서는
	 * 								{@code IllegalArgumentException}이라고 잘못 적었다).
	 * @see #parseExpr(String)
	 */
	public static DefaultSubmodelReference parseSubmodelExpr(String exprStr) {
		return DefaultSubmodelReference.parseStringExpr(exprStr);
	}
	
	/**
	 * 표현식 문자열로부터 {@link MDTElementReference}를 파싱한다.
	 * <p>
	 * 선두 라벨에 따라 참조 종류가 결정된다.
	 * <ul>
	 *   <li>{@code param:<instanceId>:<paramSpec>} → {@link MDTParameterReference}
	 *       (단, {@code paramSpec}이 {@code "*"}이면 {@link MDTParameterCollectionReference})</li>
	 *   <li>{@code oparg:<submodelSpec>:(in|out):<argSpec>} → {@link MDTArgumentReference}</li>
	 *   <li>{@code opvar:<submodelSpec>:<idShortPath>:(in|out|inout):<ordinal>}
	 *       → {@link OperationVariableReference}</li>
	 *   <li>{@code timeseries:<submodelSpec>[#<range>][|<columns>]} → {@link TimeSeriesRecordsReference}
	 *       (range: {@code last=N}(마지막 N개) | {@code last=dur[@now|@latest]} | {@code from~to})</li>
	 *   <li>그 외(라벨 없음) {@code <submodelSpec>:<elementPath>} → {@link DefaultElementReference}</li>
	 * </ul>
	 * {@code submodelSpec}은 {@code <instanceId>:[idShort=]<submodelIdShort>} 또는
	 * {@code submodel:id=<submodelId>} 형식이다.
	 *
	 * @param refExpr	ElementReference 표현식.
	 * @return 파싱된 {@link MDTElementReference} 객체.
	 * @throws IllegalArgumentException	{@code refExpr}이 {@code null}이거나, 문법은 맞지만 참조로 만들 수 없는 경우.
	 * @throws MDTParserException	표현식의 문법이 틀렸거나 뒤에 남는 입력이 있는 경우(2026-11-01 이전에는 후행 입력만
	 * 								{@code IllegalArgumentException}이었다).
	 */
	public static MDTElementReference parseExpr(String refExpr) {
		Preconditions.checkNotNullArgument(refExpr, "ElementReference expression is null");

		// 모든 참조 표현식이 evaluate()로 MDTElementReference를 만든다. 표현식 종류별 분기는 필요 없다.
		return MDTExpressionParser.parseElementReference(refExpr).evaluate();
	}
	
	// ElementReference 타입별 deserializer 레지스트리(불변). 새로운 ElementReference 타입을 추가하는 경우
	// 반드시 여기에 등록해야 polymorphic JSON 역직렬화(parseJsonNode)가 가능하다.
	// 같은 타입 이름을 두 번 등록하면 클래스 초기화 때 IllegalArgumentException이 난다.
	private static final MultiTypeJsonNodeDeserializer<ElementReference> DESERIALIZER
		= MultiTypeJsonNodeDeserializer.builder(ElementReference.class)
			.register(DefaultElementReference.SERIALIZATION_TYPE, DefaultElementReference::deserializeFields)
			.register(MDTParameterReference.SERIALIZATION_TYPE, ElementReferences::deserializeParameterReference)
			.register(MDTArgumentReference.SERIALIZATION_TYPE, MDTArgumentReference::deserializeFields)
			.register(OperationVariableReference.SERIALIZATION_TYPE, OperationVariableReference::deserializeFields)
			.register(FileStoreReference.SERIALIZATION_TYPE, FileStoreReference::deserializeFields)
			.register(TimeSeriesRecordsReference.SERIALIZATION_TYPE, TimeSeriesRecordsReference::deserializeFields)
			.build();
	// Serializer가 쓰는 타입 이름 필드. 레지스트리가 읽는 필드와 항상 같다.
	private static final String FIELD_TYPE = DESERIALIZER.getTypeField();

	// 'mdt:ref:param' 타입은 parameterExpr 필드 값에 따라 개별 파라미터('*' 이외)와
	// 파라미터 집합('*') 참조로 나뉜다.
	private static ElementReference deserializeParameterReference(JsonNode jnode) throws IOException {
		String paramExpr = JacksonUtils.getTextField(jnode, MDTParameterReference.FIELD_PARAMETER_EXPR);
		return paramExpr.equals("*")
				? MDTParameterCollectionReference.deserializeFields(jnode)
				: MDTParameterReference.deserializeFields(jnode);
	}

	/**
	 * {@link ElementReference}를 {@code @type} 필드와 함께 Json으로 직렬화하는 Jackson serializer.
	 */
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<ElementReference> {
		private Serializer() {
			super(ElementReference.class);
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
	public static class Deserializer extends JsonNodeStdDeserializer<ElementReference> {
		public Deserializer() {
			super(ElementReference.class, DESERIALIZER);
		}
	}

}
