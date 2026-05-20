package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		Assertions.assertEquals(VALUE_JSON, json);
		Assertions.assertEquals(VALUE_SERIALIZATION, value.toValueJsonString());
	}

	@Test
	public void deserializeNamedProperty() throws IOException {
		ElementValue value = ElementValues.parseJsonString(VALUE_JSON);
		Assertions.assertTrue(value instanceof OperationVariableValue);
		OperationVariableValue opVarValue = (OperationVariableValue)value;
		
		List<ElementValue> inputs = opVarValue.getInputValues();
		Assertions.assertEquals(2, inputs.size());
		Assertions.assertEquals(0, ((PropertyValue)inputs.get(0)).toValueObject());
		Assertions.assertEquals(1, ((PropertyValue)inputs.get(1)).toValueObject());
		
		List<ElementValue> outputs = opVarValue.getOutputValues();
		Assertions.assertEquals(1, outputs.size());
		Assertions.assertEquals(0, ((PropertyValue)inputs.get(0)).toValueObject());
		
		List<ElementValue> inoutputs = opVarValue.getInoutputValues();
		Assertions.assertEquals(0, inoutputs.size());
	}
}
