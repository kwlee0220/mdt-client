package mdt.model.instance;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;

import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class PropertyParameterInfo extends DefaultParameterInfo {
	@JsonProperty("value") private final String m_value;

	public static PropertyParameterInfo from(String id, Property prop) {
		return new PropertyParameterInfo(id, DataTypes.fromAas4jDatatype(prop.getValueType()).getId(), prop.getValue());
	}

	@JsonCreator
	public PropertyParameterInfo(@JsonProperty("id") String id,
								@JsonProperty("type") String type,
								@JsonProperty("value") String value) {
		super(id, type);
		
		m_value = value;
	}

	public String getValue() {
		return m_value;
	}
	
	@Override
	public String toValueString() {
		return (this.m_value != null) ? this.m_value : "None";
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("id", getId());
		gen.writeStringField("type", getType());
		gen.writeStringField("value", m_value);
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("Property{%s=%s}", getId(), toValueString());
	}
	
	public static final void main(String... args) throws Exception {
		DefaultProperty prop = new DefaultProperty();
		prop.setIdShort("param1");
		prop.setValue("123");
		prop.setValueType(DataTypeDefXsd.INT);
		
		PropertyParameterInfo param = PropertyParameterInfo.from("param1", prop);
		String jsonStr = MDTModelSerDe.toJsonString(param);
		System.out.println(jsonStr);
		
		String jsonStr2 = """
		{
			"id": "param2",
			"type": "xs:string",
			"value": "testValue"
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr2, PropertyParameterInfo.class));
		
		String jsonStr3 = """
		{
			"id": "task3",
			"type": "xs:float",
			"value": null
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr3, PropertyParameterInfo.class));
		
		String jsonStr4 = """
		{
			"id": "task4",
			"type": "xs:float"
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr4, PropertyParameterInfo.class));
	}
}
