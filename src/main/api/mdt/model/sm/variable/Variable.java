package mdt.model.sm.variable;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.sm.value.ElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = Variables.Serializer.class)
@JsonDeserialize(using = Variables.Deserializer.class)
public interface Variable {
	public String getName();
	public String getDescription();
	
	public SubmodelElement read() throws IOException;
	public ElementValue readValue() throws IOException;
	
	public void update(SubmodelElement sme) throws IOException;
	public void updateValue(ElementValue value) throws IOException;
	public void updateWithValueJsonString(String valueJsonString) throws IOException;

	public String getSerializationType();
	public void serializeFields(JsonGenerator gen) throws IOException;
}
