package mdt.model.sm.ref;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		MDTArgumentReference ref = m_mapper.readValue(JSON_STRING, MDTArgumentReference.class);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals(DefaultSubmodelReference.ofIdShort("test", "Simulation"), ref.getSubmodelReference());
		Assert.assertEquals(MDTArgumentKind.INPUT, ref.getKind());
		Assert.assertEquals("1", ref.getArgumentSpec());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assert.assertTrue(ref instanceof MDTArgumentReference);
		
		MDTArgumentReference argRef = (MDTArgumentReference)ref;
		Assert.assertTrue(argRef.getSubmodelReference() instanceof ByIdShortSubmodelReference);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals(MDTArgumentKind.OUTPUT, argRef.getKind());
		Assert.assertEquals("SleepTime", argRef.getArgumentSpec());
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, "SleepTime");
		Assert.assertEquals(EXPR, ref.toStringExpr());
	}
}
