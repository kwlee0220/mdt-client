package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class OperationVariableValue implements ElementValue {
    private final List<ElementValue> m_inputValues;
    private final List<ElementValue> m_outputValues;
    private final List<ElementValue> m_inoutputValues;
    
    public OperationVariableValue(List<ElementValue> inValues, List<ElementValue> outValues, List<ElementValue> inoutValues) {
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

	public static final String SERIALIZATION_TYPE = "mdt:value:opvars";
	private static final String FIELD_INPUT_VARIABLES = "inputs";
	private static final String FIELD_OUTPUT_VARIABLES = "outputs";
	private static final String FIELD_INOUTPUT_VARIABLES = "inoutputs";
	
	public static OperationVariableValue parseJsonNode(JsonNode jnode, OperationVariableValue proto) throws IOException {
		List<ElementValue> inValues;
		if ( proto.m_inoutputValues.size() > 0 ) {
			Class<? extends ElementValue> elmCls = proto.m_inoutputValues.get(0).getClass();
			inValues = ElementListValue.parseJsonNode(jnode.get(FIELD_INPUT_VARIABLES), elmCls).getElementAll();
		}
		else {
			inValues = List.of();
		}
		
		List<ElementValue> outValues;
		if ( proto.m_outputValues.size() > 0 ) {
			Class<? extends ElementValue> elmCls = proto.m_outputValues.get(0).getClass();
			outValues = ElementListValue.parseJsonNode(jnode.get(FIELD_OUTPUT_VARIABLES), elmCls).getElementAll();
		}
		else {
			outValues = List.of();
		}
		
		List<ElementValue> inoutValues;
		if ( proto.m_inoutputValues.size() > 0 ) {
			Class<? extends ElementValue> elmCls = proto.m_inoutputValues.get(0).getClass();
			inoutValues = ElementListValue.parseJsonNode(jnode.get(FIELD_INOUTPUT_VARIABLES), elmCls).getElementAll();
		}
		else {
			inoutValues = List.of();
		}
		
		return new OperationVariableValue(inValues, outValues, inoutValues);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		
		gen.writeFieldName(FIELD_INPUT_VARIABLES);
		new ElementListValue(m_inputValues).serialize(gen);
		gen.writeFieldName(FIELD_OUTPUT_VARIABLES);
		new ElementListValue(m_outputValues).serialize(gen);
		gen.writeFieldName(FIELD_INOUTPUT_VARIABLES);
		new ElementListValue(m_inoutputValues).serialize(gen);
		
		gen.writeEndObject();
	}
}
