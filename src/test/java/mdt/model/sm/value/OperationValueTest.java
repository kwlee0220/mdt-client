package mdt.model.sm.value;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON
	= "{\"inputs\":[\"0\",\"1\"],\"outputs\":[\"0\"],\"inoutputs\":[]}";
	
	@Test
	public void serializeNamedProperty() throws JsonProcessingException {
		PropertyValue data = new PropertyValue("0");
		PropertyValue incAmount = new PropertyValue("1");
		OperationVariableValue value = new OperationVariableValue(List.of(data, incAmount), List.of(data), List.of());
		
		String json = m_mapper.writeValueAsString(value);
		Assert.assertEquals(VALUE_JSON, json);
	}

//	@Test
//	public void deserializeNamedProperty() throws JsonMappingException, JsonProcessingException {
//		OperationValue value = m_mapper.readValue(VALUE_JSON, OperationValue.class);
//		Assert.assertEquals("id1", value.getIdShort());
//		
//		List<ElementValue> inputs = value.getInputValues();
//		Assert.assertEquals(2, inputs.size());
//		Assert.assertEquals("0", ((PropertyValue)inputs.get(0)).get());
//		Assert.assertEquals("1", ((PropertyValue)inputs.get(1)).get());
//		
//		List<ElementValue> outputs = value.getOutputValues();
//		Assert.assertEquals(1, outputs.size());
//		Assert.assertEquals("0", ((PropertyValue)inputs.get(0)).get());
//		
//		List<ElementValue> inoutputs = value.getInoutputValues();
//		Assert.assertEquals(0, inoutputs.size());
//	}
}
