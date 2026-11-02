package mdt.model.sm.ref;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.json.JacksonDeserializationException;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTParserException;
import mdt.model.sm.ref.timeseries.TimeSeriesRecordsReference;
import mdt.model.sm.value.ElementValues;

/**
 * {@link ElementReferences}의 표현식 파싱 / Json 직렬화 / 활성화 동작에 대한 단위 테스트.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementReferencesTest {

	// --- parseExpr: 라벨별 참조 종류 ---

	@Test
	public void parseExpr_default_returnsDefaultElementReference() {
		ElementReference ref = ElementReferences.parseExpr("inst:Data:DataInfo.Equipment.EquipmentID");
		Assertions.assertTrue(ref instanceof DefaultElementReference);
		Assertions.assertEquals(
				DefaultElementReference.newInstance("inst", "Data", "DataInfo.Equipment.EquipmentID"), ref);
	}

	@Test
	public void parseExpr_param_returnsParameterReference() {
		ElementReference ref = ElementReferences.parseExpr("param:inst:Temperature");
		Assertions.assertTrue(ref instanceof MDTParameterReference);
		Assertions.assertEquals(MDTParameterReference.newInstance("inst", "Temperature"), ref);
	}

	@Test
	public void parseExpr_paramStar_returnsParameterCollectionReference() {
		ElementReference ref = ElementReferences.parseExpr("param:inst:*");
		Assertions.assertTrue(ref instanceof MDTParameterCollectionReference);
		Assertions.assertEquals(MDTParameterCollectionReference.newInstance("inst"), ref);
	}

	@Test
	public void parseExpr_oparg_returnsArgumentReference() {
		ElementReference ref = ElementReferences.parseExpr("oparg:inst:Simulation:in:0");
		Assertions.assertTrue(ref instanceof MDTArgumentReference);
		Assertions.assertEquals(
				MDTArgumentReference.newInstance(DefaultSubmodelReference.ofIdShort("inst", "Simulation"),
													MDTArgumentKind.INPUT, "0"),
				ref);
	}

	@Test
	public void parseExpr_opvar_returnsOperationVariableReference() {
		ElementReference ref = ElementReferences.parseExpr("opvar:inst:AddAndSleep:Operation:out:1");
		Assertions.assertTrue(ref instanceof OperationVariableReference);
		Assertions.assertEquals(
				OperationVariableReference.newInstance(
						DefaultElementReference.newInstance(
								DefaultSubmodelReference.ofIdShort("inst", "AddAndSleep"), "Operation"),
						OperationVariableReference.Kind.OUTPUT, 1),
				ref);
	}

	@Test
	public void parseExpr_timeseries_returnsTimeSeriesRecordsReference() {
		ElementReference ref = ElementReferences.parseExpr("timeseries:inst:Data");
		Assertions.assertTrue(ref instanceof TimeSeriesRecordsReference);
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"),
								((TimeSeriesRecordsReference)ref).getSubmodelReference());
	}

	@Test
	public void parseExpr_null_throws() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.parseExpr(null));
	}

	// --- parseSubmodelExpr ---

	@Test
	public void parseSubmodelExpr_idShort() {
		DefaultSubmodelReference smRef = ElementReferences.parseSubmodelExpr("inst:Data");
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), smRef);
	}

	@Test
	public void parseSubmodelExpr_submodelId() {
		DefaultSubmodelReference smRef = ElementReferences.parseSubmodelExpr("submodel:id=mysm");
		Assertions.assertEquals(DefaultSubmodelReference.ofId("mysm"), smRef);
	}

	@Test
	public void parseSubmodelExpr_errors() {
		// 문법 오류는 MDTParserException이다(이전 문서는 IllegalArgumentException이라고 적었다).
		Assertions.assertThrows(MDTParserException.class, () -> ElementReferences.parseSubmodelExpr("inst"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.parseSubmodelExpr(null));
	}

	// --- parseTimeSeriesRecordsReference ---

	@Test
	public void parseTimeSeries_plain() {
		TimeSeriesRecordsReference ref = (TimeSeriesRecordsReference)ElementReferences.parseExpr("timeseries:inst:Data");
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), ref.getSubmodelReference());
	}

	@Test
	public void parseTimeSeries_length() throws IOException {
		// 'last=N'(접미사 없는 정수) = 마지막 N개 레코드: JSON 'last' 필드는 숫자
		TimeSeriesRecordsReference ref
				= (TimeSeriesRecordsReference)ElementReferences.parseExpr("timeseries:inst:Data#last=7");
		Assertions.assertEquals(7, ref.toJsonNode().get("last").asInt());
	}

	@Test
	public void parseTimeSeries_duration() throws IOException {
		TimeSeriesRecordsReference ref
				= (TimeSeriesRecordsReference)ElementReferences.parseExpr("timeseries:inst:Data#last=30s@now");
		// Trailing 기간은 JSON 'last' 필드에 "<기간>@<anchor>" 문자열로 직렬화된다.
		Assertions.assertEquals("PT30S@now", ref.toJsonNode().get("last").asText());
	}

	@Test
	public void parseTimeSeries_columns() throws IOException {
		TimeSeriesRecordsReference ref
				= (TimeSeriesRecordsReference)ElementReferences.parseExpr("timeseries:inst:Data|current,power");
		JsonNode cols = ref.toJsonNode().get("columns");
		Assertions.assertEquals(2, cols.size());
		Assertions.assertEquals("current", cols.get(0).asText());
		Assertions.assertEquals("power", cols.get(1).asText());
	}

	@Test
	public void parseTimeSeries_lengthAndColumns() throws IOException {
		TimeSeriesRecordsReference ref
				= (TimeSeriesRecordsReference)ElementReferences.parseExpr("timeseries:inst:Data#last=3|current");
		JsonNode node = ref.toJsonNode();
		Assertions.assertEquals(3, node.get("last").asInt());
		Assertions.assertEquals(1, node.get("columns").size());
		Assertions.assertEquals("current", node.get("columns").get(0).asText());
	}

	@Test
	public void parseTimeSeries_invalidRangeKey_throws() {
		// 후행 입력도 문법 오류(MDTParserException)다. 2026-11-01 이전에는 IllegalArgumentException이었다.
		Assertions.assertThrows(MDTParserException.class,
				() -> ElementReferences.parseExpr("timeseries:inst:Data(bogus=1):"));
	}

	@Test
	public void parseExpr_trailingInput_isParserException() {
		MDTParserException e = Assertions.assertThrows(MDTParserException.class,
															() -> ElementReferences.parseExpr("inst:Data:a b"));
		Assertions.assertTrue(e.getMessage().startsWith("Unexpected trailing input at 1:12 - 'b'"), e.getMessage());
		Assertions.assertThrows(MDTParserException.class, () -> ElementValues.parseExpr("a b"));
		Assertions.assertThrows(MDTParserException.class, () -> ElementReferences.parseSubmodelExpr("inst:Data x"));
	}

	// --- Json 직렬화/역직렬화 ---

	@Test
	public void parseJsonString_roundTrip() throws IOException {
		ElementReference ref = DefaultElementReference.newInstance("inst", "Data", "DataInfo.x");
		String json = ref.toJsonString();
		Assertions.assertEquals(ref, ElementReferences.parseJsonString(json));
	}

	@Test
	public void parseJsonNode_roundTrip() throws IOException {
		ElementReference ref = DefaultElementReference.newInstance("inst", "Data", "DataInfo.x");
		Assertions.assertEquals(ref, ElementReferences.parseJsonNode(ref.toJsonNode()));
	}

	@Test
	public void parseJsonNode_missingType_throws() {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.put("instanceId", "inst");
		Assertions.assertThrows(JacksonDeserializationException.class,
								() -> ElementReferences.parseJsonNode(node));
	}

	/**
	 * 구현체가 던진 {@link IOException}이 그대로 전달되어야 한다.
	 * (이전에는 Jackson {@code treeToValue}를 거치면서 {@link IllegalArgumentException}으로 바뀌었다.)
	 */
	@Test
	public void parseJsonNode_missingRequiredField_throwsIOException() throws IOException {
		ElementReference ref = DefaultElementReference.newInstance("inst", "Data", "DataInfo.x");
		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.remove("elementPath");
		IOException e = Assertions.assertThrows(IOException.class, () -> ElementReferences.parseJsonNode(node));
		Assertions.assertTrue(e.getMessage().contains("elementPath"), e.getMessage());
	}

	/**
	 * {@code null}은 거부한다. (이전에는 {@code null}과 JSON {@code null} 노드에 대해 예외 없이 {@code null}을 반환했다.)
	 */
	@Test
	public void parseJsonNode_null_throws() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.parseJsonNode(null));
		Assertions.assertThrows(JacksonDeserializationException.class,
								() -> ElementReferences.parseJsonNode(NullNode.getInstance()));
	}

	/**
	 * {@code parseJsonString}은 {@code parseJsonNode}와 같이 동작한다. 이전에는 {@code "null"}이면 {@code null}을
	 * 돌려주고, 뒤에 붙은 입력을 무시했으며, 오류 메시지에 JSON 전체를 넣었다.
	 */
	@Test
	public void parseJsonString_strict() throws IOException {
		Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementReferences.parseJsonString("null"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.parseJsonString(null));
		String json = MDTParameterReference.newInstance("inst", "Speed").toJsonString();
		Assertions.assertThrows(IOException.class, () -> ElementReferences.parseJsonString(json + " garbage"));
		Assertions.assertThrows(IOException.class, () -> ElementReferences.parseJsonString("{"));

		// 필드 누락 메시지는 필드 이름만 담는다(이전에는 JSON 전체를 담은 IOException).
		String noPath = "{\"@type\":\"" + DefaultElementReference.SERIALIZATION_TYPE + "\","
						+ "\"submodelReference\":{\"instanceId\":\"secret-inst\",\"submodelIdShort\":\"Data\"}}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementReferences.parseJsonString(noPath));
		Assertions.assertTrue(e.getMessage().contains("elementPath"), e.getMessage());
		Assertions.assertFalse(e.getMessage().contains("secret-inst"), e.getMessage());
	}

	/**
	 * 문자열 필드는 JSON 문자열만 받는다. 이전에는 JSON {@code null}을 {@code "null"}로, 숫자를 문자열로 받아
	 * 오류 없이 참조를 만들었다. 오류 메시지에는 필드 이름만 들어가고 값은 들어가지 않는다.
	 */
	@Test
	public void parseJsonNode_stringFieldRejectsNullAndNumber() throws IOException {
		ObjectNode node = (ObjectNode)MDTParameterReference.newInstance("secret-inst", "Speed").toJsonNode();
		node.putNull("instanceId");
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementReferences.parseJsonNode(node));
		Assertions.assertTrue(e.getMessage().contains("instanceId"), e.getMessage());

		node.put("instanceId", 123);
		e = Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementReferences.parseJsonNode(node));
		Assertions.assertTrue(e.getMessage().contains("instanceId"), e.getMessage());
		Assertions.assertFalse(e.getMessage().contains("Speed"), e.getMessage());

		ObjectNode elm = (ObjectNode)DefaultElementReference.newInstance("inst", "Data", "DataInfo.x").toJsonNode();
		elm.put("elementPath", 1);
		Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementReferences.parseJsonNode(elm));
	}

	/**
	 * 중첩된 Submodel 참조의 식별자도 비어 있지 않은 JSON 문자열만 받는다. 이전에는 {@code true}·{@code 42}를
	 * 문자열로 받아 {@code true:42:x} 참조를 만들었다.
	 */
	@Test
	public void parseJsonNode_nestedSubmodelReferenceIdsMustBeText() throws IOException {
		String json = "{\"@type\":\"mdt:ref:element\",\"submodelReference\":{\"instanceId\":true,"
						+ "\"submodelIdShort\":42},\"elementPath\":\"x\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementReferences.parseJsonString(json));
		Assertions.assertTrue(e.getMessage().contains("submodelIdShort"), e.getMessage());

		String blank = "{\"@type\":\"mdt:ref:element\",\"submodelReference\":{\"instanceId\":\" \","
						+ "\"submodelIdShort\":\"Data\"},\"elementPath\":\"x\"}";
		e = Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementReferences.parseJsonString(blank));
		Assertions.assertEquals("invalid field 'instanceId': blank", e.getMessage());

		String missing = "{\"@type\":\"mdt:ref:element\",\"submodelReference\":{\"instanceId\":\"secret-inst\"},"
						+ "\"elementPath\":\"x\"}";
		e = Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementReferences.parseJsonString(missing));
		Assertions.assertFalse(e.getMessage().contains("secret-inst"), e.getMessage());

		// 생성자도 같은 규칙이다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> DefaultSubmodelReference.ofIdShort("", "Data"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> DefaultSubmodelReference.ofId(" "));
	}

	/**
	 * {@code ordinal}은 JSON 정수만 받는다(직렬화 형식). 이전에는 문자열 {@code "1"}도 받았다.
	 */
	@Test
	public void parseJsonNode_opvarOrdinalMustBeInt() throws IOException {
		ElementReference ref = ElementReferences.parseExpr("opvar:inst:AddAndSleep:Operation:out:1");
		Assertions.assertEquals(ref, ElementReferences.parseJsonNode(ref.toJsonNode()));

		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.put("ordinal", "1");
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementReferences.parseJsonNode(node));
		Assertions.assertTrue(e.getMessage().contains("ordinal"), e.getMessage());
	}

	// --- 공유 "mdt:ref:param" 타입의 Json 디스패치 (parameterExpr 기준 분기) ---

	/**
	 * {@code parameterExpr="*"}인 {@code mdt:ref:param}은 {@link MDTParameterCollectionReference}로 복원되어야 한다.
	 */
	@Test
	public void parseJson_paramStar_dispatchesToCollection() throws IOException {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		ElementReference restored = ElementReferences.parseJsonString(ref.toJsonString());
		Assertions.assertTrue(restored instanceof MDTParameterCollectionReference);
		Assertions.assertEquals(ref, restored);
	}

	/**
	 * {@code parameterExpr}이 {@code "*"}가 아닌 {@code mdt:ref:param}은 {@link MDTParameterReference}로 복원되어야 한다.
	 */
	@Test
	public void parseJson_paramSpec_dispatchesToParameter() throws IOException {
		MDTParameterReference ref = MDTParameterReference.newInstance("inst", "Temperature");
		ElementReference restored = ElementReferences.parseJsonString(ref.toJsonString());
		Assertions.assertTrue(restored instanceof MDTParameterReference);
		Assertions.assertEquals(ref, restored);
	}

	/**
	 * 두 클래스는 같은 {@code @type}을 공유하지만 서로 다른 종류로 복원되어야 한다.
	 */
	@Test
	public void parseJson_param_sharedTypeButDistinctKinds() throws IOException {
		Assertions.assertEquals(MDTParameterReference.SERIALIZATION_TYPE,
								MDTParameterCollectionReference.SERIALIZATION_TYPE);

		ElementReference coll = ElementReferences.parseJsonString(
									MDTParameterCollectionReference.newInstance("inst").toJsonString());
		ElementReference param = ElementReferences.parseJsonString(
									MDTParameterReference.newInstance("inst", "X").toJsonString());
		Assertions.assertTrue(coll instanceof MDTParameterCollectionReference);
		Assertions.assertTrue(param instanceof MDTParameterReference);
	}
}
