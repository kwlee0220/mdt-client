package mdt.model.sm.ref;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
	public void testToStringExpr2() throws JsonProcessingException {
		MDTParameterReference ref = MDTParameterReference.newInstance("welder", "NozzleProduction.DefectVolume");
		Assertions.assertEquals(EXPR2, ref.toStringExpr());
	}
}
