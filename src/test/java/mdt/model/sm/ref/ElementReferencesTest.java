package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.json.JacksonDeserializationException;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.timeseries.TimeSeriesElementReference;

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
	public void parseExpr_timeseries_returnsTimeSeriesElementReference() {
		ElementReference ref = ElementReferences.parseExpr("timeseries:inst:Data");
		Assertions.assertTrue(ref instanceof TimeSeriesElementReference);
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"),
								((TimeSeriesElementReference)ref).getSubmodelReference());
	}

	@Test
	public void parseExpr_null_throws() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.parseExpr(null));
	}

	// --- parseSubmodelReference ---

	@Test
	public void parseSubmodelReference_idShort() {
		DefaultSubmodelReference smRef = ElementReferences.parseSubmodelReference("inst:Data");
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), smRef);
	}

	@Test
	public void parseSubmodelReference_submodelId() {
		DefaultSubmodelReference smRef = ElementReferences.parseSubmodelReference("submodel:mysm");
		Assertions.assertEquals(DefaultSubmodelReference.ofId("mysm"), smRef);
	}

	// --- parseTimeSeriesElementReference ---

	@Test
	public void parseTimeSeries_plain() {
		TimeSeriesElementReference ref = (TimeSeriesElementReference)ElementReferences.parseExpr("timeseries:inst:Data");
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), ref.getSubmodelReference());
	}

	@Test
	public void parseTimeSeries_length() throws IOException {
		// 'last=N'(접미사 없는 정수) = 마지막 N개 레코드: JSON 'last' 필드는 숫자
		TimeSeriesElementReference ref
				= (TimeSeriesElementReference)ElementReferences.parseExpr("timeseries:inst:Data#last=7");
		Assertions.assertEquals(7, ref.toJsonNode().get("last").asInt());
	}

	@Test
	public void parseTimeSeries_duration() throws IOException {
		TimeSeriesElementReference ref
				= (TimeSeriesElementReference)ElementReferences.parseExpr("timeseries:inst:Data#last=30s@now");
		// Trailing 기간은 JSON 'last' 필드에 "<기간>@<anchor>" 문자열로 직렬화된다.
		Assertions.assertEquals("PT30S@now", ref.toJsonNode().get("last").asText());
	}

	@Test
	public void parseTimeSeries_columns() throws IOException {
		TimeSeriesElementReference ref
				= (TimeSeriesElementReference)ElementReferences.parseExpr("timeseries:inst:Data|current,power");
		JsonNode cols = ref.toJsonNode().get("columns");
		Assertions.assertEquals(2, cols.size());
		Assertions.assertEquals("current", cols.get(0).asText());
		Assertions.assertEquals("power", cols.get(1).asText());
	}

	@Test
	public void parseTimeSeries_lengthAndColumns() throws IOException {
		TimeSeriesElementReference ref
				= (TimeSeriesElementReference)ElementReferences.parseExpr("timeseries:inst:Data#last=3|current");
		JsonNode node = ref.toJsonNode();
		Assertions.assertEquals(3, node.get("last").asInt());
		Assertions.assertEquals(1, node.get("columns").size());
		Assertions.assertEquals("current", node.get("columns").get(0).asText());
	}

	@Test
	public void parseTimeSeries_invalidRangeKey_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> ElementReferences.parseExpr("timeseries:inst:Data(bogus=1):"));
	}

	// --- Json 직렬화/역직렬화 ---

	@Test
	public void parseJsonString_roundTrip() throws IOException {
		ElementReference ref = DefaultElementReference.newInstance("inst", "Data", "DataInfo.x");
		String json = ref.toJsonString();
		Assertions.assertEquals(ref, ElementReferences.parseJsonString(json));
	}

	@Test
	public void parseTypedJsonNode_missingType_throws() {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.put("instanceId", "inst");
		Assertions.assertThrows(JacksonDeserializationException.class,
								() -> ElementReferences.parseTypedJsonNode(node));
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

	// --- activate ---

	@Test
	public void activate_nullArguments_throw() {
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		ElementReference ref = DefaultElementReference.newInstance("inst", "Data", "x");
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.activate(null, manager));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementReferences.activate(ref, null));
	}

	@Test
	public void activate_nonMdtElementReference_throws() {
		ElementReference ref = mock(ElementReference.class);
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> ElementReferences.activate(ref, manager));
	}
}
