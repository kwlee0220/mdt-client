package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class FileValueTest {
	private static final String JSON
		= "{\"@type\":\"mdt:value:file\",\"value\":{\"contentType\":\"application/jpeg\",\"value\":\"/home/kwlee/image.jpg\"}}";
	private static final String VALUE_JSON
		= "{\"contentType\":\"application/jpeg\",\"value\":\"/home/kwlee/image.jpg\"}";
	
	@Test
	public void testSerializeFileValue() throws IOException {
		FileValue nev = new FileValue("application/jpeg", "/home/kwlee/image.jpg");
		
//		System.out.println(nev.toJson());
		Assert.assertEquals(JSON, nev.toJsonString());
		Assert.assertEquals(VALUE_JSON, nev.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assert.assertTrue(value instanceof FileValue);
		FileValue fileValue = (FileValue)value;
		Assert.assertEquals("/home/kwlee/image.jpg", fileValue.getValue());
		Assert.assertEquals("application/jpeg", fileValue.getMimeType());
	}

	@Test
	public void testUpdateWithRawString() throws IOException {
		DefaultFile aasFile = new DefaultFile();
		ElementValues.updateWithValueJsonString(aasFile, VALUE_JSON);
		Assert.assertEquals("/home/kwlee/image.jpg", aasFile.getValue());
		Assert.assertEquals("application/jpeg", aasFile.getContentType());
	}
}
