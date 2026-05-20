package mdt.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NameValueTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String JSON = "{\"name\":\"nickName\",\"value\":\"Tommy\"}";

	@Test
	public void serdeNameValue() throws IOException {
		NameValue pair = NameValue.of("nickName", "Tommy");

		JsonNode written = MAPPER.readTree(MAPPER.writeValueAsString(pair));
		Assertions.assertEquals(MAPPER.readTree(JSON), written);

		NameValue read = MAPPER.readValue(JSON, NameValue.class);
		Assertions.assertEquals("nickName", read.getName());
		Assertions.assertEquals("Tommy", read.getValue());
	}

	@Test
	public void equalsAndHashCodeDependOnNameAndValue() {
		NameValue pair = NameValue.of("nickName", "Tommy");
		NameValue same = NameValue.of("nickName", "Tommy");
		NameValue differentValue = NameValue.of("nickName", "Thomas");
		NameValue differentName = NameValue.of("other", "nickName");

		Assertions.assertEquals(pair, same);
		Assertions.assertEquals(pair.hashCode(), same.hashCode());
		Assertions.assertNotEquals(pair, differentValue);
		Assertions.assertNotEquals(pair, differentName);

		Set<NameValue> set = new HashSet<>();
		set.add(pair);
		Assertions.assertTrue(set.contains(same));
		Assertions.assertFalse(set.contains(differentValue));
		Assertions.assertFalse(set.contains(differentName));
	}
}