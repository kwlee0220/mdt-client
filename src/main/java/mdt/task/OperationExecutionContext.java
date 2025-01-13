package mdt.task;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.sm.ref.ElementReference;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationExecutionContext {
	private KeyedValueList<String,Parameter> m_inputParameters = KeyedValueList.newInstance(Parameter::getName);
	private KeyedValueList<String,Parameter> m_outputParameters = KeyedValueList.newInstance(Parameter::getName);
	private ElementReference m_lastExecutinTimeRef;
	
	public KeyedValueList<String,Parameter> getInputParameters() {
		return m_inputParameters;
	}
	
	public void setInputParameters(Iterable<Parameter> parameters) {
		m_inputParameters = FStream.from(parameters)
									.collect(KeyedValueList.newInstance(Parameter::getName), KeyedValueList::add);
	}
	
	public KeyedValueList<String,Parameter> getOutputParameters() {
		return m_outputParameters;
	}
	
	public void setOutputParameters(Iterable<Parameter> parameters) {
		m_outputParameters = FStream.from(parameters)
									.collect(KeyedValueList.newInstance(Parameter::getName), KeyedValueList::add);
	}
	
	public ElementReference getLastExecutionTimeReference() {
		return m_lastExecutinTimeRef;
	}
	
	public void setLastExecutionTimeReference(ElementReference ref) {
		m_lastExecutinTimeRef = ref;
	}
	
	@Override
	public String toString() {
		String inParamsStr = FStream.from(m_inputParameters).map(Parameter::getName).join(", ", "{", "}");
		String outParamsStr = FStream.from(m_outputParameters).map(Parameter::getName).join(", ", "{", "}");
		
		return String.format("inputs=%s, outputs=%s", inParamsStr, outParamsStr);
	}
}
