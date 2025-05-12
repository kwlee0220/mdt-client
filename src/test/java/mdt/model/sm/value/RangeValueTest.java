package mdt.model.sm.value;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RangeValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON = "{\"min\":\"1\",\"max\":\"10\"}";
	
	@Test
	public void serializeNamedProperty() throws JsonProcessingException {
		RangeValue value = new RangeValue("1", "10");
		
		String json = m_mapper.writeValueAsString(value);
		System.out.println(json);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void testParseJsonNode() throws JsonMappingException, JsonProcessingException {
		RangeValue value = RangeValue.parseJsonNode(m_mapper.readTree(VALUE_JSON));
		Assert.assertEquals("1", value.getMin());
		Assert.assertEquals("10", value.getMax());
	}
}
