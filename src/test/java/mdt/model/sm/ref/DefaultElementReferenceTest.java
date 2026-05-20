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
		Assertions.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		DefaultElementReference ref = m_mapper.readValue(JSON_STRING, DefaultElementReference.class);
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("test", "Simulation"), ref.getSubmodelReference());
		Assertions.assertEquals("DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue", ref.getIdShortPathString());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "Simulation");
		DefaultElementReference ref = DefaultElementReference.newInstance(smRef,
													"DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue");
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}
}
