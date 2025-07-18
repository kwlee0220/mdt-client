package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class OperationVariableValue extends AbstractElementValue implements ElementValue {
    private final List<ElementValue> m_inputValues;
    private final List<ElementValue> m_outputValues;
    private final List<ElementValue> m_inoutputValues;
    
    public OperationVariableValue(List<ElementValue> inValues, List<ElementValue> outValues,
    								List<ElementValue> inoutValues) {
    	m_inputValues = inValues;
    	m_outputValues = outValues;
    	m_inoutputValues = inoutValues;
    }
    
	public List<ElementValue> getInputValues() {
		return m_inputValues;
	}
	
	public List<ElementValue> getOutputValues() {
		return m_outputValues;
	}
	
	public List<ElementValue> getInoutputValues() {
		return m_inoutputValues;
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}

	@Override
	public Object toValueJsonObject() {
		Map<String, Object> value = Maps.newLinkedHashMap();
		value.put(FIELD_INPUT_VARIABLES, new ElementListValue(m_inputValues).toValueJsonObject());
		value.put(FIELD_OUTPUT_VARIABLES, new ElementListValue(m_outputValues).toValueJsonObject());
		value.put(FIELD_INOUTPUT_VARIABLES, new ElementListValue(m_inoutputValues).toValueJsonObject());
		return value;
	}

	public static final String SERIALIZATION_TYPE = "mdt:value:opvars";
	private static final String FIELD_INPUT_VARIABLES = "inputs";
	private static final String FIELD_OUTPUT_VARIABLES = "outputs";
	private static final String FIELD_INOUTPUT_VARIABLES = "inoutputs";

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	public static OperationVariableValue deserializeValue(JsonNode vnode) {
		JsonNode inVarsNode = JacksonUtils.getFieldOrNull(vnode, FIELD_INPUT_VARIABLES);
		List<ElementValue> inVars = ((ElementListValue)ElementValues.parseJsonNode(inVarsNode)).getElementAll();
		
		JsonNode outVarsNode = JacksonUtils.getFieldOrNull(vnode, FIELD_OUTPUT_VARIABLES);
		List<ElementValue> outVars = ((ElementListValue)ElementValues.parseJsonNode(outVarsNode)).getElementAll();
		
		JsonNode inoutVarsNode = JacksonUtils.getFieldOrNull(vnode, FIELD_INOUTPUT_VARIABLES);
		List<ElementValue> inoutVars = ((ElementListValue)ElementValues.parseJsonNode(inoutVarsNode)).getElementAll();
		
		return new OperationVariableValue(inVars, outVars, inoutVars);
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		
		gen.writeFieldName(FIELD_INPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_inputValues), gen);
		
		gen.writeFieldName(FIELD_OUTPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_outputValues), gen);
		
		gen.writeFieldName(FIELD_INOUTPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_inoutputValues), gen);

		gen.writeEndObject();
	}
}
