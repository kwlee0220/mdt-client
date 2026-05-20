package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		FileValue nev = new FileValue("/home/kwlee/image.jpg", "application/jpeg");
		
//		System.out.println(nev.toJson());
		Assertions.assertEquals(JSON, nev.toJsonString());
		Assertions.assertEquals(VALUE_JSON, nev.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assertions.assertTrue(value instanceof FileValue);
		FileValue fileValue = (FileValue)value;
		Assertions.assertEquals("/home/kwlee/image.jpg", fileValue.getValue());
		Assertions.assertEquals("application/jpeg", fileValue.getMimeType());
	}

	@Test
	public void testUpdateWithRawString() throws IOException {
		DefaultFile aasFile = new DefaultFile();
		ElementValues.updateWithValueJsonString(aasFile, VALUE_JSON);
		Assertions.assertEquals("/home/kwlee/image.jpg", aasFile.getValue());
		Assertions.assertEquals("application/jpeg", aasFile.getContentType());
	}
}
