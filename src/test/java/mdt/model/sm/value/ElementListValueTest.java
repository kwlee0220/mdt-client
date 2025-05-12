package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementListValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON = "[\"abc\",\"def\"]";
	
	@Test
	public void testSerialize() throws JsonProcessingException {
		PropertyValue pv1 = new PropertyValue("abc");
		PropertyValue pv2 = new PropertyValue("def");
		ElementListValue value = new ElementListValue(List.of(pv1, pv2));
		
		String json = m_mapper.writeValueAsString(value);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementListValue value = ElementListValue.parseJsonNode(m_mapper.readTree(VALUE_JSON), PropertyValue.class);
		List<ElementValue> elements = value.getElementAll();
		
		Assert.assertEquals("abc", ((PropertyValue)elements.get(0)).get());
		Assert.assertEquals("def", ((PropertyValue)elements.get(1)).get());
	}
}
