package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.model.ModelValidationException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:param\",\"instanceId\":\"test\",\"parameterExpr\":\"IncAmount\"}";
	private static final String EXPR = "param:test:SleepTime";
	
	private static final String JSON_STRING2
	= "{\"@type\":\"mdt:ref:param\",\"instanceId\":\"welder\",\"parameterExpr\":\"NozzleProduction.DefectVolume\"}";
	private static final String EXPR2 = "param:welder:NozzleProduction.DefectVolume";
	
	@Test
	public void testSerializeMDTParameterReference() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "IncAmount");
		
		String json = m_mapper.writeValueAsString(ref);
		Assertions.assertEquals(JSON_STRING, json);
	}
	
	@Test
	public void testSerializeMDTParameterReference2() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("welder", "NozzleProduction.DefectVolume");
		
		String json = m_mapper.writeValueAsString(ref);
		System.out.println(json);
		Assertions.assertEquals(JSON_STRING2, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		MDTParameterReference ref = m_mapper.readValue(JSON_STRING, MDTParameterReference.class);
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals("IncAmount", ref.getParameterExpr());
	}

	@Test
	public void testDeserialize2() throws JsonMappingException, JsonProcessingException {
		MDTParameterReference ref = m_mapper.readValue(JSON_STRING2, MDTParameterReference.class);
		Assertions.assertEquals("welder", ref.getInstanceId());
		Assertions.assertEquals("NozzleProduction.DefectVolume", ref.getParameterExpr());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assertions.assertTrue(ref instanceof MDTParameterReference);
		
		MDTParameterReference paramRef = (MDTParameterReference)ref;
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals("SleepTime", paramRef.getParameterExpr());
	}

	@Test
	public void testExpr() {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}
	
	@Test
	public void testParseExpr2() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR2);
		
		Assertions.assertTrue(ref instanceof MDTParameterReference);
		
		MDTParameterReference paramRef = (MDTParameterReference)ref;
		Assertions.assertEquals("welder", paramRef.getInstanceId());
		Assertions.assertEquals("NozzleProduction.DefectVolume", paramRef.getParameterExpr());
	}

	@Test
	public void testExpr2() {
		MDTParameterReference ref = MDTParameterReference.newInstance("welder", "NozzleProduction.DefectVolume");
		
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR2, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		Assertions.assertEquals(EXPR, ref.toStringExpr());
	}

	@Test
	public void jsonRoundTrip_preservesReference() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("welder", "NozzleProduction.DefectVolume");
		String json = m_mapper.writeValueAsString(ref);
		MDTParameterReference restored = m_mapper.readValue(json, MDTParameterReference.class);
		Assertions.assertEquals(ref, restored);
	}
	
	@Test
	public void testToStringExpr2() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("welder", "NozzleProduction.DefectVolume");
		Assertions.assertEquals(EXPR2, ref.toStringExpr());
	}

	// --- 기본 속성 / 검증 ---

	@Test
	public void getSerializationType_isParamType() {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		Assertions.assertEquals("mdt:ref:param", ref.getSerializationType());
		Assertions.assertEquals("mdt:ref:param", MDTParameterReference.SERIALIZATION_TYPE);
	}

	@Test
	public void newInstance_nullParameterExpr_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> MDTParameterReference.newInstance("test", null));
	}

	@Test
	public void newInstance_nullInstanceId_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> MDTParameterReference.newInstance(null, "SleepTime"));
	}

	// --- equals / hashCode ---

	@Test
	public void equals_sameValues_areEqual() {
		MDTParameterReference r1 = MDTParameterReference.newInstance("test", "SleepTime");
		MDTParameterReference r2 = MDTParameterReference.newInstance("test", "SleepTime");
		Assertions.assertEquals(r1, r2);
		Assertions.assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void equals_differentExprOrInstance_areNotEqual() {
		MDTParameterReference base = MDTParameterReference.newInstance("test", "SleepTime");
		Assertions.assertNotEquals(base, MDTParameterReference.newInstance("test", "IncAmount"));
		Assertions.assertNotEquals(base, MDTParameterReference.newInstance("other", "SleepTime"));
		Assertions.assertNotEquals(base, null);
		Assertions.assertNotEquals(base, "not-a-reference");
	}

	// --- buildIdShortPath (활성화 후 idShort path 생성) ---

	@Test
	public void buildIdShortPath_numericIndex_machine() {
		MDTInstance instance = mockInstance(MDTAssetType.Machine);
		MDTParameterReference ref = activate("inst", "0", instance);
		Assertions.assertEquals("DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue",
								ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_numericIndex_process() {
		MDTInstance instance = mockInstance(MDTAssetType.Process);
		MDTParameterReference ref = activate("inst", "2", instance);
		Assertions.assertEquals("DataInfo.Operation.OperationParameterValues[2].ParameterValue",
								ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_namedParameter_resolvesIndex() {
		MDTInstance instance = mockInstance(MDTAssetType.Machine);
		ParameterCollection paramColl = mock(ParameterCollection.class);
		when(instance.getParameterCollection()).thenReturn(paramColl);
		when(paramColl.getParameterIndex("Temperature")).thenReturn(3);

		MDTParameterReference ref = activate("inst", "Temperature", instance);
		Assertions.assertEquals("DataInfo.Equipment.EquipmentParameterValues[3].ParameterValue",
								ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_namedWithDotSubPath() {
		MDTInstance instance = mockInstance(MDTAssetType.Machine);
		ParameterCollection paramColl = mock(ParameterCollection.class);
		when(instance.getParameterCollection()).thenReturn(paramColl);
		when(paramColl.getParameterIndex("Temperature")).thenReturn(3);

		MDTParameterReference ref = activate("inst", "Temperature.unit", instance);
		Assertions.assertEquals("DataInfo.Equipment.EquipmentParameterValues[3].ParameterValue.unit",
								ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_namedWithBracketSubPath() {
		MDTInstance instance = mockInstance(MDTAssetType.Machine);
		ParameterCollection paramColl = mock(ParameterCollection.class);
		when(instance.getParameterCollection()).thenReturn(paramColl);
		when(paramColl.getParameterIndex("Samples")).thenReturn(1);

		MDTParameterReference ref = activate("inst", "Samples[2]", instance);
		Assertions.assertEquals("DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue[2]",
								ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_nullAssetType_throwsModelValidation() {
		MDTInstance instance = mockInstance(null);
		MDTInstanceManager manager = managerFor("inst", instance);
		MDTParameterReference ref = MDTParameterReference.newInstance("inst", "0");

		Assertions.assertThrows(ModelValidationException.class, () -> ref.activate(manager));
	}

	@Test
	public void buildIdShortPath_unsupportedAssetType_throwsIllegalArgument() {
		MDTInstance instance = mockInstance(MDTAssetType.Line);
		MDTInstanceManager manager = managerFor("inst", instance);
		MDTParameterReference ref = MDTParameterReference.newInstance("inst", "0");

		Assertions.assertThrows(IllegalArgumentException.class, () -> ref.activate(manager));
	}

	// --- 헬퍼 ---

	private static MDTInstance mockInstance(MDTAssetType assetType) {
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getAssetType()).thenReturn(assetType);
		return instance;
	}

	private static MDTInstanceManager managerFor(String instanceId, MDTInstance instance) {
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance(instanceId)).thenReturn(instance);
		return manager;
	}

	private static MDTParameterReference activate(String instanceId, String expr, MDTInstance instance) {
		MDTParameterReference ref = MDTParameterReference.newInstance(instanceId, expr);
		ref.activate(managerFor(instanceId, instance));
		return ref;
	}
}
