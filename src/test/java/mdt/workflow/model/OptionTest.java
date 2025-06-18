package mdt.workflow.model;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OptionTest {
	ObjectMapper m_mapper = new ObjectMapper();
	
	private static final String PROP_JSON_STRING = """
		{"@type":"mdt:option:string","name":"nickName","value":"Tommy"}""";
	
	@Test
	public void serdeStringOption() throws IOException {
		StringOption opt = new StringOption("nickName", "Tommy");
		
		String jsonStr = m_mapper.writeValueAsString(opt);
		Assert.assertEquals(PROP_JSON_STRING, jsonStr);
		
		Option<?> read = m_mapper.readValue(jsonStr, Option.class);
		Assert.assertTrue(read instanceof StringOption);
		StringOption opt2 = (StringOption)read;
		Assert.assertEquals("nickName", opt2.getName());
		Assert.assertEquals("Tommy", opt2.getValue());
	}
}
