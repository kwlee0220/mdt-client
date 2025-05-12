package mdt.client.operation;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.variable.Variable;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "operation", "inputVariables", "outputVariables", "async" })
public class OperationRequest {
	private String m_opId;
	private KeyedValueList<String,Variable> m_inputVariables = KeyedValueList.with(Variable::getName);
	private KeyedValueList<String,Variable> m_outputVariables = KeyedValueList.with(Variable::getName);
	private boolean m_async = true;
	
	public String getOperation() {
		return m_opId;
	}
	
	public void setOperation(String id) {
		m_opId = id;
	}
	
	public KeyedValueList<String,Variable> getInputVariables() {
		return m_inputVariables;
	}
	
	public void setInputVariables(List<Variable> variables) {
		m_inputVariables = KeyedValueList.from(variables, Variable::getName);
	}
	
	public KeyedValueList<String,Variable> getOutputVariables() {
		return m_outputVariables;
	}
	
	public void setOutputVariables(List<Variable> variables) {
		m_outputVariables = KeyedValueList.from(variables, Variable::getName);
	}
	
	public boolean isAsync() {
		return m_async;
	}
	
	public void setAsync(boolean async) {
		m_async = async;
	}
	
	public static OperationRequest parseJsonString(String jsonStr) throws IOException {
		return MDTModelSerDe.readValue(jsonStr, OperationRequest.class);
	}
	
	public String toString() {
		String inPortNames = FStream.from(this.m_inputVariables.keySet()).join(", ");
		String outPortNames = FStream.from(this.m_outputVariables.keySet()).join(", ");

		return String.format("(%s) -> (%s)", inPortNames, outPortNames);
	}
}
