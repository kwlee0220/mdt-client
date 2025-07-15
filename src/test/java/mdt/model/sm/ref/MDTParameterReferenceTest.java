package mdt.model.sm.ref;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:param\",\"instanceId\":\"test\",\"parameterId\":\"IncAmount\"}";
	private static final String EXPR = "param:test:SleepTime";
	
	private static final String JSON_STRING2
	= "{\"@type\":\"mdt:ref:param\",\"instanceId\":\"test\",\"parameterId\":\"Data\",\"subPath\":\"ParameterValue.intValue\"}";
	private static final String EXPR2 = "param:test:Data:ParameterValue.intValue";
	
	@Test
	public void testSerializeMDTParameterReference() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "IncAmount");
		
		String json = m_mapper.writeValueAsString(ref);
		Assert.assertEquals(JSON_STRING, json);
	}
	
	@Test
	public void testSerializeMDTParameterReference2() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "Data", "ParameterValue.intValue");
		
		String json = m_mapper.writeValueAsString(ref);
		System.out.println(json);
		Assert.assertEquals(JSON_STRING2, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		MDTParameterReference ref = m_mapper.readValue(JSON_STRING, MDTParameterReference.class);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals("IncAmount", ref.getParameterId());
	}

	@Test
	public void testDeserialize2() throws JsonMappingException, JsonProcessingException {
		MDTParameterReference ref = m_mapper.readValue(JSON_STRING2, MDTParameterReference.class);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals("Data", ref.getParameterId());
		Assert.assertNotNull(ref.getSubPath());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assert.assertTrue(ref instanceof MDTParameterReference);
		
		MDTParameterReference paramRef = (MDTParameterReference)ref;
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals("SleepTime", paramRef.getParameterId());
		Assert.assertNull(paramRef.getSubPath());
	}

	@Test
	public void testExpr() {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		
		String expr = ref.toStringExpr();
		Assert.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assert.assertEquals(ref, ref2);
	}
	
	@Test
	public void testParseExpr2() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR2);
		
		Assert.assertTrue(ref instanceof MDTParameterReference);
		
		MDTParameterReference paramRef = (MDTParameterReference)ref;
		Assert.assertEquals("test", paramRef.getInstanceId());
		Assert.assertEquals("Data", paramRef.getParameterId());
		Assert.assertEquals("ParameterValue.intValue", paramRef.getSubPath());
	}

	@Test
	public void testExpr2() {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "Data", "ParameterValue.intValue");
		
		String expr = ref.toStringExpr();
		Assert.assertEquals(EXPR2, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assert.assertEquals(ref, ref2);
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		Assert.assertEquals(EXPR, ref.toStringExpr());
	}
	
	@Test
	public void testToStringExpr2() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "Data", "ParameterValue.intValue");
		Assert.assertEquals(EXPR2, ref.toStringExpr());
	}
}
