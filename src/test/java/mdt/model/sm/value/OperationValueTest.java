package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationValueTest {
	private static final String VALUE_JSON
		= "{\"@type\":\"mdt:value:opvars\",\"value\":{"
			+ "\"inputs\":{\"@type\":\"mdt:value:list\",\"value\":["
			+ "{\"@type\":\"mdt:value:integer\",\"value\":0},"
			+ "{\"@type\":\"mdt:value:integer\",\"value\":1}]},"
			+ "\"outputs\":{\"@type\":\"mdt:value:list\",\"value\":["
			+ "{\"@type\":\"mdt:value:integer\",\"value\":0}]},"
			+ "\"inoutputs\":{\"@type\":\"mdt:value:list\",\"value\":[]}}}";
	private static final String VALUE_SERIALIZATION
	= "{\"inputs\":[0,1],\"outputs\":[0],\"inoutputs\":[]}";
	
	@Test
	public void serializeNamedProperty() throws IOException {
		PropertyValue<Integer> data = PropertyValue.INTEGER(0);
		PropertyValue<Integer> incAmount = PropertyValue.INTEGER(1);
		OperationVariableValue value = new OperationVariableValue(List.of(data, incAmount), List.of(data), List.of());
		
		String json = value.toJsonString();
//		System.out.println(json);
		Assert.assertEquals(VALUE_JSON, json);
		Assert.assertEquals(VALUE_SERIALIZATION, value.toValueJsonString());
	}

	@Test
	public void deserializeNamedProperty() throws IOException {
		ElementValue value = ElementValues.parseJsonString(VALUE_JSON);
		Assert.assertTrue(value instanceof OperationVariableValue);
		OperationVariableValue opVarValue = (OperationVariableValue)value;
		
		List<ElementValue> inputs = opVarValue.getInputValues();
		Assert.assertEquals(2, inputs.size());
		Assert.assertEquals(0, ((PropertyValue)inputs.get(0)).toValueObject());
		Assert.assertEquals(1, ((PropertyValue)inputs.get(1)).toValueObject());
		
		List<ElementValue> outputs = opVarValue.getOutputValues();
		Assert.assertEquals(1, outputs.size());
		Assert.assertEquals(0, ((PropertyValue)inputs.get(0)).toValueObject());
		
		List<ElementValue> inoutputs = opVarValue.getInoutputValues();
		Assert.assertEquals(0, inoutputs.size());
	}
}
