package mdt.client.operation;

import java.io.IOException;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import utils.stream.FStream;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "operation", "inputArguments", "outputArguments", "async" })
public class OperationRequest {
	@JsonProperty("operation") private String m_opId;
	@JsonProperty("inputArguments")
	private Map<String,SubmodelElement> m_inputArguments = Maps.newHashMap();
	@JsonProperty("outputArguments")
	private Map<String,SubmodelElement> m_outputArguments = Maps.newHashMap();
	@JsonProperty("async") private boolean m_async = true;
	
	public String getOperation() {
		return m_opId;
	}
	
	public void setOperation(String id) {
		m_opId = id;
	}
	
	public Map<String,SubmodelElement> getInputArguments() {
		return m_inputArguments;
	}
	
	public void setInputArguments(Map<String,SubmodelElement> arguments) {
		m_inputArguments = Maps.newHashMap(arguments);
	}
	
	public Map<String,SubmodelElement> getOutputArguments() {
		return m_outputArguments;
	}
	
	public void setOutputArguments(Map<String,SubmodelElement> arguments) {
		m_outputArguments = Maps.newHashMap(arguments);
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
	
	public String toJsonString() throws IOException {
		return MDTModelSerDe.toJsonString(this);
	}
	
	public String toString() {
		String inArgNames = FStream.from(m_inputArguments.keySet()).join(", ");
		String outArgNames = FStream.from(m_outputArguments.keySet()).join(", ");

		return String.format("(%s) -> (%s)", inArgNames, outArgNames);
	}
}
