package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.model.sm.simulation.Simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTArgumentReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:oparg\","
				+ "\"submodelReference\":{\"instanceId\":\"test\",\"submodelIdShort\":\"Simulation\"},"
				+ "\"kind\":\"input\",\"argumentSpec\":\"1\"}";
	private static final String EXPR = "oparg:test:Simulation:out:SleepTime";
	
	@Test
	public void testSerializeMDTArgumentReference() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.INPUT, "1");
		
		String json = m_mapper.writeValueAsString(ref);
		Assertions.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		MDTArgumentReference ref = m_mapper.readValue(JSON_STRING, MDTArgumentReference.class);
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("test", "Simulation"), ref.getSubmodelReference());
		Assertions.assertEquals(MDTArgumentKind.INPUT, ref.getKind());
		Assertions.assertEquals("1", ref.getArgumentSpec());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assertions.assertTrue(ref instanceof MDTArgumentReference);
		
		MDTArgumentReference argRef = (MDTArgumentReference)ref;
		Assertions.assertTrue(argRef.getSubmodelReference() instanceof ByIdShortSubmodelReference);
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals(MDTArgumentKind.OUTPUT, argRef.getKind());
		Assertions.assertEquals("SleepTime", argRef.getArgumentSpec());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, "SleepTime");
		
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, "SleepTime");
		Assertions.assertEquals(EXPR, ref.toStringExpr());
	}

	@Test
	public void jsonRoundTrip_preservesReference() throws JsonProcessingException {
		MDTArgumentReference ref = MDTArgumentReference.newInstance(
				DefaultSubmodelReference.ofIdShort("test", "Simulation"), MDTArgumentKind.OUTPUT, "SleepTime");
		String json = m_mapper.writeValueAsString(ref);
		MDTArgumentReference restored = m_mapper.readValue(json, MDTArgumentReference.class);
		Assertions.assertEquals(ref, restored);
	}

	// --- 기본 속성 / 검증 ---

	@Test
	public void getSerializationType_isOpArgType() {
		MDTArgumentReference ref = newRef(MDTArgumentKind.INPUT, "0");
		Assertions.assertEquals("mdt:ref:oparg", ref.getSerializationType());
		Assertions.assertEquals("mdt:ref:oparg", MDTArgumentReference.SERIALIZATION_TYPE);
	}

	@Test
	public void accessors_returnConstructorArguments() {
		MDTArgumentReference ref = newRef(MDTArgumentKind.OUTPUT, "Result");
		Assertions.assertEquals(MDTArgumentKind.OUTPUT, ref.getKind());
		Assertions.assertEquals("Result", ref.getArgumentSpec());
	}

	@Test
	public void newInstance_nullKind_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MDTArgumentReference.newInstance(DefaultSubmodelReference.ofIdShort("test", "Simulation"),
														null, "0"));
	}

	@Test
	public void newInstance_nullArgSpec_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MDTArgumentReference.newInstance(DefaultSubmodelReference.ofIdShort("test", "Simulation"),
														MDTArgumentKind.INPUT, null));
	}

	@Test
	public void newInstance_nullSubmodelRef_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MDTArgumentReference.newInstance(null, MDTArgumentKind.INPUT, "0"));
	}

	// --- builder ---

	@Test
	public void builder_buildsEquivalentReference() {
		MDTArgumentReference built = MDTArgumentReference.builder()
													.submodelReference(DefaultSubmodelReference.ofIdShort("test", "Simulation"))
													.kind(MDTArgumentKind.INPUT)
													.argument("0")
													.build();
		Assertions.assertEquals(newRef(MDTArgumentKind.INPUT, "0"), built);
	}

	// --- equals / hashCode ---

	@Test
	public void equals_sameValues_areEqual() {
		MDTArgumentReference r1 = newRef(MDTArgumentKind.INPUT, "0");
		MDTArgumentReference r2 = newRef(MDTArgumentKind.INPUT, "0");
		Assertions.assertEquals(r1, r2);
		Assertions.assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void equals_differentFields_areNotEqual() {
		MDTArgumentReference base = newRef(MDTArgumentKind.INPUT, "0");
		Assertions.assertNotEquals(base, newRef(MDTArgumentKind.OUTPUT, "0"));
		Assertions.assertNotEquals(base, newRef(MDTArgumentKind.INPUT, "1"));
		Assertions.assertNotEquals(base,
				MDTArgumentReference.newInstance(DefaultSubmodelReference.ofIdShort("other", "Simulation"),
													MDTArgumentKind.INPUT, "0"));
		Assertions.assertNotEquals(base, null);
		Assertions.assertNotEquals(base, "not-a-reference");
	}

	// --- buildIdShortPath (활성화 후 idShort path 생성) ---

	@Test
	public void buildIdShortPath_allArgs_returnsListPrefix() {
		MDTArgumentReference ref = activate(MDTArgumentKind.INPUT, "*", Simulation.SEMANTIC_ID);
		Assertions.assertEquals("SimulationInfo.Inputs", ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_numericIndex_output() {
		MDTArgumentReference ref = activate(MDTArgumentKind.OUTPUT, "1", Simulation.SEMANTIC_ID);
		Assertions.assertEquals("SimulationInfo.Outputs[1].OutputValue", ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_aiSemanticId_input() {
		MDTArgumentReference ref = activate(MDTArgumentKind.INPUT, "0", AI.SEMANTIC_ID);
		Assertions.assertEquals("AIInfo.Inputs[0].InputValue", ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_unknownSemanticId_throws() {
		MDTArgumentReference ref = newRef(MDTArgumentKind.INPUT, "0");
		MDTInstanceManager manager = managerWith(new MDTSubmodelDescriptor("sm1", "Simulation", "urn:unknown"));

		Assertions.assertThrows(IllegalArgumentException.class, () -> ref.activate(manager));
	}

	@Test
	public void buildIdShortPath_submodelNotFound_throws() {
		MDTArgumentReference ref = newRef(MDTArgumentKind.INPUT, "0");
		MDTInstanceManager manager = managerWith(new MDTSubmodelDescriptor("sm1", "Other", Simulation.SEMANTIC_ID));

		Assertions.assertThrows(ResourceNotFoundException.class, () -> ref.activate(manager));
	}

	// --- 헬퍼 ---

	private static MDTArgumentReference newRef(MDTArgumentKind kind, String argSpec) {
		return MDTArgumentReference.newInstance(DefaultSubmodelReference.ofIdShort("test", "Simulation"),
												kind, argSpec);
	}

	private static MDTInstanceManager managerWith(MDTSubmodelDescriptor... descriptors) {
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getMDTSubmodelDescriptorAll()).thenReturn(List.of(descriptors));
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance("test")).thenReturn(instance);
		return manager;
	}

	private static MDTArgumentReference activate(MDTArgumentKind kind, String argSpec, String semanticId) {
		MDTArgumentReference ref = newRef(kind, argSpec);
		ref.activate(managerWith(new MDTSubmodelDescriptor("sm1", "Simulation", semanticId)));
		return ref;
	}
}
