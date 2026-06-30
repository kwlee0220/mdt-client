package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;

/**
 * {@link DefaultElementReference}의 단위 테스트.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultElementReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String PATH = "DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue";
	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:element\","
				+ "\"submodelReference\":{\"instanceId\":\"test\",\"submodelIdShort\":\"Simulation\"},"
				+ "\"elementPath\":\"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue\"}";
	private static final String EXPR = "test:Simulation:DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue";

	@Test
	public void testSerializeDefaultElementReference() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef, PATH);

		String json = m_mapper.writeValueAsString(ref);
		Assertions.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		DefaultElementReference ref = m_mapper.readValue(JSON_STRING, DefaultElementReference.class);
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("test", "Simulation"), ref.getSubmodelReference());
		Assertions.assertEquals(PATH, ref.getIdShortPathString());
	}

	/**
	 * {@link DefaultElementReference}는 상위 클래스와 달리 활성화 없이도 idShort path를 반환해야 한다.
	 */
	@Test
	public void getIdShortPathString_worksWithoutActivation() {
		DefaultElementReference ref = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);
		Assertions.assertEquals(PATH, ref.getIdShortPathString());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef, PATH);
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR, expr);

		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}

	// --- 생성/접근자 ---

	@Test
	public void buildIdShortPath_returnsElementPath() {
		DefaultElementReference ref = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);
		Assertions.assertEquals(PATH, ref.buildIdShortPath());
	}

	@Test
	public void getSubmodelReference_returnsConstructorArgument() {
		MDTSubmodelReference smRef = mock(MDTSubmodelReference.class);
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef, PATH);
		Assertions.assertSame(smRef, ref.getSubmodelReference());
	}

	@Test
	public void getSerializationType_isElementType() {
		DefaultElementReference ref = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);
		Assertions.assertEquals("mdt:ref:element", ref.getSerializationType());
		Assertions.assertEquals("mdt:ref:element", DefaultElementReference.SERIALIZATION_TYPE);
	}

	@Test
	public void newInstance_nullPath_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), null));
	}

	@Test
	public void newInstance_nullSubmodelRef_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> DefaultElementReference.newInstance((MDTSubmodelReference)null, PATH));
	}

	@Test
	public void newInstance_byInstanceIdAndIdShort_buildsExpectedReference() {
		DefaultElementReference ref = DefaultElementReference.newInstance("inst", "Data", "a.b");
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), ref.getSubmodelReference());
		Assertions.assertEquals("a.b", ref.buildIdShortPath());
	}

	// --- child ---

	@Test
	public void child_appendsNameToPathAndSharesSubmodelRef() {
		MDTSubmodelReference smRef = mock(MDTSubmodelReference.class);
		DefaultElementReference parent = DefaultElementReference.newInstance(smRef, "DataInfo.Equipment");

		DefaultElementReference child = parent.child("EquipmentID");

		Assertions.assertEquals("DataInfo.Equipment.EquipmentID", child.buildIdShortPath());
		Assertions.assertSame(smRef, child.getSubmodelReference());
	}

	// --- toStringExpr ---

	@Test
	public void toStringExpr_combinesSubmodelExprAndPath() {
		MDTSubmodelReference smRef = mock(MDTSubmodelReference.class);
		when(smRef.toStringExpr()).thenReturn("inst:Data");

		DefaultElementReference ref = DefaultElementReference.newInstance(smRef, "a.b");
		Assertions.assertEquals("inst:Data:a.b", ref.toStringExpr());
	}

	// --- equals / hashCode ---

	@Test
	public void equals_sameSubmodelRefAndPath_areEqual() {
		MDTSubmodelReference smRef = mock(MDTSubmodelReference.class);
		DefaultElementReference r1 = DefaultElementReference.newInstance(smRef, PATH);
		DefaultElementReference r2 = DefaultElementReference.newInstance(smRef, PATH);

		Assertions.assertEquals(r1, r2);
		Assertions.assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void equals_differentPath_areNotEqual() {
		MDTSubmodelReference smRef = mock(MDTSubmodelReference.class);
		DefaultElementReference r1 = DefaultElementReference.newInstance(smRef, "a.b");
		DefaultElementReference r2 = DefaultElementReference.newInstance(smRef, "a.c");

		Assertions.assertNotEquals(r1, r2);
	}

	@Test
	public void equals_differentSubmodelRef_areNotEqual() {
		DefaultElementReference r1 = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);
		DefaultElementReference r2 = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);

		Assertions.assertNotEquals(r1, r2);
	}

	@Test
	public void equals_nullOrOtherType_isFalse() {
		DefaultElementReference ref = DefaultElementReference.newInstance(mock(MDTSubmodelReference.class), PATH);
		Assertions.assertNotEquals(ref, null);
		Assertions.assertNotEquals(ref, "not-a-reference");
	}

	// --- JSON 직렬화/역직렬화 ---

	/**
	 * 직렬화 후 역직렬화하면 동치인 {@link DefaultElementReference}로 복원되어야 한다.
	 */
	@Test
	public void jsonRoundTrip_preservesReference() throws IOException {
		DefaultElementReference ref = DefaultElementReference.newInstance("inst", "Data", PATH);

		String json = ref.toJsonString();
		ElementReference restored = ElementReferences.parseJsonString(json);

		Assertions.assertTrue(restored instanceof DefaultElementReference);
		Assertions.assertEquals(ref, restored);
	}

	/**
	 * {@code deserializeFields}는 두 필드를 읽어 참조를 복원해야 한다.
	 */
	@Test
	public void deserializeFields_buildsReferenceFromFields() throws IOException {
		ObjectNode node = newFieldsNode(DefaultSubmodelReference.ofIdShort("inst", "Data"), PATH);

		DefaultElementReference ref = DefaultElementReference.deserializeFields(node);
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), ref.getSubmodelReference());
		Assertions.assertEquals(PATH, ref.buildIdShortPath());
	}

	/**
	 * {@code parseJson}은 {@code deserializeFields}에 위임하므로 같은 결과를 내야 한다.
	 */
	@Test
	public void parseJson_delegatesToDeserializeFields() throws IOException {
		ObjectNode node = newFieldsNode(DefaultSubmodelReference.ofIdShort("inst", "Data"), PATH);

		DefaultElementReference ref = DefaultElementReference.parseJson(node);
		Assertions.assertEquals(DefaultElementReference.newInstance("inst", "Data", PATH), ref);
	}

	@Test
	public void deserializeFields_missingSubmodelReference_throws() {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.put("elementPath", PATH);

		Assertions.assertThrows(IOException.class, () -> DefaultElementReference.deserializeFields(node));
	}

	@Test
	public void deserializeFields_missingElementPath_throws() {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.set("submodelReference", MDTModelSerDe.toJsonNode(DefaultSubmodelReference.ofIdShort("inst", "Data")));

		Assertions.assertThrows(IOException.class, () -> DefaultElementReference.deserializeFields(node));
	}

	@Test
	public void deserializeFields_nullSubmodelReference_throwsIllegalArgument() {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.putNull("submodelReference");
		node.put("elementPath", PATH);

		Assertions.assertThrows(IllegalArgumentException.class,
								() -> DefaultElementReference.deserializeFields(node));
	}

	// --- 헬퍼 ---

	private static ObjectNode newFieldsNode(MDTSubmodelReference smRef, String elementPath) {
		ObjectNode node = MDTModelSerDe.getJsonMapper().createObjectNode();
		node.set("submodelReference", MDTModelSerDe.toJsonNode(smRef));
		node.put("elementPath", elementPath);
		return node;
	}
}
