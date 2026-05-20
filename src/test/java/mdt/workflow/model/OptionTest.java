package mdt.workflow.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OptionTest {
	ObjectMapper m_mapper = new ObjectMapper();
	
	private static final String PROP_JSON_STRING = """
		{"name":"nickName","value":"Tommy"}""";
	
	@Test
	public void serdeStringOption() throws IOException {
		Option opt = new Option("nickName", "Tommy");
		
		String jsonStr = m_mapper.writeValueAsString(opt);
		Assertions.assertEquals(PROP_JSON_STRING, jsonStr);
		
		Option read = m_mapper.readValue(jsonStr, Option.class);
		Assertions.assertEquals("nickName", read.getName());
		Assertions.assertEquals("Tommy", read.getValue());
	}
}
