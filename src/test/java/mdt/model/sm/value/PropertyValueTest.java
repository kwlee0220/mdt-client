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
public class PropertyValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON = "\"abc\"";
	
	@Test
	public void testSerializePropertyValue() throws JsonProcessingException {
		PropertyValue value = new PropertyValue("abc");
		
		String json = m_mapper.writeValueAsString(value);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void deserializeNamedProperty() throws JsonMappingException, JsonProcessingException {
		PropertyValue value = m_mapper.readValue(VALUE_JSON, PropertyValue.class);
		Assert.assertEquals("abc", value.get());
	}
}
