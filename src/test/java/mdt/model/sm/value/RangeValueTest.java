package mdt.model.sm.value;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.aas.DataTypes;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RangeValueTest {
	private static final String JSON
		= "{\"@type\":\"mdt:value:range\",\"value\":{\"vtype\":\"INT\",\"min\":1,\"max\":10}}";
	private static final String VALUE_JSON = "{\"min\":1,\"max\":10}";
	
	@Test
	public void serializeNamedProperty() throws IOException {
		RangeValue<Integer> value = new RangeValue<>(DataTypes.INT, 1, 10);

//		System.out.println(value.toJson());
		Assertions.assertEquals(JSON, value.toJsonString());
		Assertions.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assertions.assertTrue(value instanceof RangeValue);
		@SuppressWarnings("unchecked")
		RangeValue<Integer> rangeValue = (RangeValue<Integer>)value;
		
		Assertions.assertEquals(Integer.valueOf(1), rangeValue.getMin());
		Assertions.assertEquals(Integer.valueOf(10), rangeValue.getMax());
	}
}
