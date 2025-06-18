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
public class DefaultElementReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:element\","
				+ "\"submodelReference\":{\"instanceId\":\"test\",\"submodelIdShort\":\"Simulation\"},"
				+ "\"elementPath\":\"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue\"}";
	private static final String EXPR = "test:Simulation:DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue";
	
	@Test
	public void testSerializeDefaultElementReference() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef,
													"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		
		String json = m_mapper.writeValueAsString(ref);
		Assert.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		DefaultElementReference ref = m_mapper.readValue(JSON_STRING, DefaultElementReference.class);
		Assert.assertEquals("test", ref.getInstanceId());
		Assert.assertEquals(DefaultSubmodelReference.ofIdShort("test", "Simulation"), ref.getSubmodelReference());
		Assert.assertEquals("DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue", ref.getIdShortPathString());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef,
													"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		String expr = ref.toStringExpr();
		Assert.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assert.assertEquals(ref, ref2);
	}
}
