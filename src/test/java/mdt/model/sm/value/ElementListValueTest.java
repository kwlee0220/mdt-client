package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementListValueTest {
	private static final String JSON
			= "{\"@type\":\"mdt:value:list\",\"value\":[{\"@type\":\"mdt:value:string\","
			+ "\"value\":\"abc\"},{\"@type\":\"mdt:value:string\",\"value\":\"def\"}]}";
	private static final String VALUE_JSON = "[\"abc\",\"def\"]";
	
	@Test
	public void testSerialize() throws IOException {
		PropertyValue<String> pv1 = PropertyValue.STRING("abc");
		PropertyValue<String> pv2 = PropertyValue.STRING("def");
		ElementListValue value = new ElementListValue(List.of(pv1, pv2));
		
		String json = value.toJsonString();
		Assert.assertEquals(JSON, json);
		Assert.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonString() throws IOException {
		PropertyValue<String> pv1 = PropertyValue.STRING("abc");
		PropertyValue<String> pv2 = PropertyValue.STRING("def");
		
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assert.assertTrue(value instanceof ElementListValue);
		ElementListValue listValue = (ElementListValue)value;
		Assert.assertEquals(2, listValue.getElementAll().size());
		Assert.assertEquals(pv1, listValue.getElementAll().get(0));
		Assert.assertEquals(pv2, listValue.getElementAll().get(1));
	}
}
