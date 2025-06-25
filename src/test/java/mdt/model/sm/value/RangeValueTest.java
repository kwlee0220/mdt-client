package mdt.model.sm.value;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals(JSON, value.toJsonString());
		Assert.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assert.assertTrue(value instanceof RangeValue);
		@SuppressWarnings("unchecked")
		RangeValue<Integer> rangeValue = (RangeValue<Integer>)value;
		
		Assert.assertEquals(Integer.valueOf(1), rangeValue.getMin());
		Assert.assertEquals(Integer.valueOf(10), rangeValue.getMax());
	}
}
