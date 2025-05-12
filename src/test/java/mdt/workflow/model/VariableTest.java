package mdt.workflow.model;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.model.sm.variable.AbstractVariable.ElementVariable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class VariableTest {
	private static final Property PROPERTY = new DefaultProperty.Builder()
																.idShort("intProp")
																.valueType(DataTypeDefXsd.DOUBLE)
																.value("25.5").build();
	
	@Test
	public void getPropertyVariable1() throws IOException {
		ElementVariable port = Variables.newInstance("test", null, PROPERTY);
		SubmodelElement sme = port.read();
		Assert.assertTrue(sme instanceof Property);
		Assert.assertEquals("intProp", sme.getIdShort());
		Assert.assertEquals("25.5", ((Property)sme).getValue());
		
	}
	
	private static final String PROP_JSON_STRING = """
		{"@type":"mdt:variable:element","name":"test","description":null,"element":{"modelType":"Property","value":"25.5","valueType":"xs:double","idShort":"intProp"}}""";
	@Test
	public void getPropertyVariable2() throws IOException {
		ElementVariable tvar = Variables.newInstance("test", null, PROPERTY);
		String jsonStr = tvar.toJsonString();
		Assert.assertEquals(PROP_JSON_STRING.trim(), toCompactJson(jsonStr));
	}
	@Test
	public void getPropertyVariable3() throws IOException {
		Variable tvar = Variables.parseJsonString(PROP_JSON_STRING);
		Assert.assertTrue(tvar instanceof ElementVariable);
		Assert.assertEquals("25.5", tvar.readValue().toString());
	} 
	
	private static final File FILE = new DefaultFile.Builder()
													.idShort("fileProp")
													.value("/abs.jpg")
													.contentType("application/jpeg")
													.build();
	private static final String FILE_VALUE_JSON = "{\"mimeType\":\"application/jpeg\",\"value\":\"/abs.jpg\"}";
	@Test
	public void getFileValue() throws IOException {
	}
	
	private String toCompactJson(String jsonStr) throws JsonMappingException, JsonProcessingException {
		return MDTModelSerDe.getJsonMapper().readTree(jsonStr).toString();
	}
}
