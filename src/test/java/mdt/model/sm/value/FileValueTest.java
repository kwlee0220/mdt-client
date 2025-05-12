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
public class FileValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String VALUE_JSON
		= "{\"contentType\":\"application/jpeg\",\"value\":\"/home/kwlee/image.jpg\"}";
	
	@Test
	public void testSerializeFileValue() throws JsonProcessingException {
		FileValue nev = new FileValue("application/jpeg", "/home/kwlee/image.jpg");
		
		String json = m_mapper.writeValueAsString(nev);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void testParseJsonNode() throws JsonMappingException, JsonProcessingException {
		FileValue value = FileValue.parseJsonNode(m_mapper.readTree(VALUE_JSON));
		Assert.assertEquals("/home/kwlee/image.jpg", value.getValue());
		Assert.assertEquals("application/jpeg", value.getMimeType());
	}
}
