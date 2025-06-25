package mdt.workflow.model;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ValueVariableTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STR
		= "{\"@type\":\"mdt:variable:value\",\"name\":\"id1\",\"description\":\"\","
		+ "\"value\":{\"@type\":\"mdt:value:integer\",\"value\":222}}";
	
	@Test
	public void testSerialize() throws JsonProcessingException {
		ValueVariable var = new ValueVariable("id1", "", PropertyValue.INTEGER(222));
		
		String json = m_mapper.writeValueAsString(var);
//		System.out.println(json);
		Assert.assertEquals(JSON_STR, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		ValueVariable value = m_mapper.readValue(JSON_STR, ValueVariable.class);
		Assert.assertEquals("id1", value.getName());
		Assert.assertEquals(PropertyValue.INTEGER(222), value.readValue());
	}
}
