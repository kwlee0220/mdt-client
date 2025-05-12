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
	
	@Test
	public void testSerializeMDTParameterReference() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "IncAmount");
		
		String json = m_mapper.writeValueAsString(ref);
		Assert.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		MDTParameterReference ref = m_mapper.readValue(JSON_STRING, MDTParameterReference.class);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals("IncAmount", ref.getParameterId());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assert.assertTrue(ref instanceof MDTParameterReference);
		
		MDTParameterReference paramRef = (MDTParameterReference)ref;
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals("SleepTime", paramRef.getParameterId());
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "SleepTime");
		Assert.assertEquals(EXPR, ref.toStringExpr());
	}
}
