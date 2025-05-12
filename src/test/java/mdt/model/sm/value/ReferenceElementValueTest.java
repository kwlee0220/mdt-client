package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ReferenceElementValueTest {
	private ObjectMapper m_mapper = new ObjectMapper();

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
	public void serializeNamedProperty() throws JsonProcessingException {
		ReferenceElementValue value = new ReferenceElementValue(REFFERENCE);
		
		String json = m_mapper.writeValueAsString(value);
		Assert.assertEquals(VALUE_JSON, json);
	}

	@Test
	public void testParseJsonNode() throws IOException {
		ReferenceElementValue value = ReferenceElementValue.parseJsonNode(m_mapper.readTree(VALUE_JSON));
		Assert.assertEquals(REFFERENCE, value.getReference());
	}
}
