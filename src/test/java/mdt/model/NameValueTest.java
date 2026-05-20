package mdt.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NameValueTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String JSON = "{\"name\":\"nickName\",\"value\":\"Tommy\"}";

	@Test
	public void serdeNameValue() throws IOException {
		NameValue pair = NameValue.of("nickName", "Tommy");

		JsonNode written = MAPPER.readTree(MAPPER.writeValueAsString(pair));
		Assert.assertEquals(MAPPER.readTree(JSON), written);

		NameValue read = MAPPER.readValue(JSON, NameValue.class);
		Assert.assertEquals("nickName", read.getName());
		Assert.assertEquals("Tommy", read.getValue());
	}

	@Test
	public void equalsAndHashCodeDependOnNameAndValue() {
		NameValue pair = NameValue.of("nickName", "Tommy");
		NameValue same = NameValue.of("nickName", "Tommy");
		NameValue differentValue = NameValue.of("nickName", "Thomas");
		NameValue differentName = NameValue.of("other", "nickName");

		Assert.assertEquals(pair, same);
		Assert.assertEquals(pair.hashCode(), same.hashCode());
		Assert.assertNotEquals(pair, differentValue);
		Assert.assertNotEquals(pair, differentName);

		Set<NameValue> set = new HashSet<>();
		set.add(pair);
		Assert.assertTrue(set.contains(same));
		Assert.assertFalse(set.contains(differentValue));
		Assert.assertFalse(set.contains(differentName));
	}
}