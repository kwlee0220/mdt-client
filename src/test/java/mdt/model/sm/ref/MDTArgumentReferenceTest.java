package mdt.model.sm.ref;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;

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
}
