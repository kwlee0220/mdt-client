package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ReferenceElementValueTest {
	private static final String JSON
		= "{\"@type\":\"mdt:value:reference\",\"value\":{\"type\":\"MODEL_REFERENCE\","
				+ "\"keys\":[{\"type\":\"SUBMODEL\",\"value\":\"http://customer.com/demo/aas/1/1/1234859590\"},"
				+ "{\"type\":\"PROPERTY\",\"value\":\"MaxRotationSpeed\"}]}}";
	private static final String VALUE_JSON
		= "{\"type\":\"MODEL_REFERENCE\",\"keys\":["
				+ "{\"type\":\"SUBMODEL\",\"value\":\"http://customer.com/demo/aas/1/1/1234859590\"},"
				+ "{\"type\":\"PROPERTY\",\"value\":\"MaxRotationSpeed\"}]}";
	private static final Reference REFFERENCE = new DefaultReference.Builder()
														.type(ReferenceTypes.MODEL_REFERENCE)
														.keys(new DefaultKey.Builder()
                                                                            .type(KeyTypes.SUBMODEL)
                                                                            .value("http://customer.com/demo/aas/1/1/1234859590")
                                                                            .build())
                                                         .keys(new DefaultKey.Builder()
                                                                            .type(KeyTypes.PROPERTY)
                                                                            .value("MaxRotationSpeed")
                                                                            .build())
                                                         .build();
	
	@Test
	public void serializeNamedProperty() throws IOException {
		ReferenceElementValue value = new ReferenceElementValue(REFFERENCE);
		
		String json = value.toJsonString();
//		System.out.println(json);
		Assert.assertEquals(JSON, json);
		Assert.assertEquals(VALUE_JSON, value.toValueJsonString());
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ElementValue value = ElementValues.parseJsonString(JSON);
		Assert.assertTrue(value instanceof ReferenceElementValue);
		ReferenceElementValue refValue = (ReferenceElementValue)value;
		Assert.assertEquals(REFFERENCE, refValue.getReference());
	}
}
