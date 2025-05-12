package mdt.model.sm.ref;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class FileStoreReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:file\",\"path\":\"/tmp/test.json\"}";
	
	@Test
	public void testSerializeMDTParameterReference() throws JsonProcessingException {
		FileStoreReference ref = new FileStoreReference(new File("/tmp/test.json"));
		
		String json = m_mapper.writeValueAsString(ref);
		Assert.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		FileStoreReference ref = m_mapper.readValue(JSON_STRING, FileStoreReference.class);
		Assert.assertEquals(new File("/tmp/test.json"), ref.getFile());
	}
}
