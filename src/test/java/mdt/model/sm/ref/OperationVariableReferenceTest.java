package mdt.model.sm.ref;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:opvar\","
				+ "\"operationReference\":{\"@type\":\"mdt:ref:element\","
				+ "\"submodelReference\":{\"instanceId\":\"test\",\"submodelIdShort\":\"AddAndSleep\"},"
				+ "\"elementPath\":\"Operation\"},\"kind\":\"inoutput\",\"ordinal\":0}";
	private static final String EXPR = "opvar:test:AddAndSleep:Operation:out:1";
	
	@Test
	public void testSerializeOperationVariableReference() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference opRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.INOUTPUT, 0);
		
		String json = m_mapper.writeValueAsString(ref);
		Assert.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		OperationVariableReference ref = (OperationVariableReference)m_mapper.readValue(JSON_STRING, ElementReference.class);
		
		Assert.assertEquals("test", ref.getInstanceId());
		DefaultElementReference opRef = (DefaultElementReference)ref.getOperationReference();
		Assert.assertEquals(DefaultSubmodelReference.ofIdShort("test", "AddAndSleep"), opRef.getSubmodelReference());
		Assert.assertEquals(Kind.INOUTPUT, ref.getVariableKind());
		Assert.assertEquals(0, ref.getVariableOrdinal());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assert.assertTrue(ref instanceof OperationVariableReference);
		
		OperationVariableReference varRef = (OperationVariableReference)ref;
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals(Kind.OUTPUT, varRef.getVariableKind());
		Assert.assertEquals(1, varRef.getVariableOrdinal());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference opRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.OUTPUT, 1);
		
		String expr = ref.toStringExpr();
		Assert.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assert.assertEquals(ref, ref2);
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference elmRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(elmRef, Kind.OUTPUT, 1);
		Assert.assertEquals(EXPR, ref.toStringExpr());
	}
}
