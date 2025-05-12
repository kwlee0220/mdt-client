package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.UnitUtils;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.variable.Variable;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({"commandLine", "workingDirectory", "operationSubmodel", "inputVariables", "outputVariables",
						"concurrent", "timeout"})
@JsonInclude(Include.NON_NULL)
public final class ProgramOperationDescriptor {
	private List<String> m_commandLine = Collections.emptyList();
	@Nullable private File m_workingDirectory;
	@Nullable private MDTSubmodelReference m_operationSubmodelRef;
	private KeyedValueList<String,Variable> m_inputVariables = KeyedValueList.with(Variable::getName);
	private KeyedValueList<String,Variable> m_outputVariables = KeyedValueList.with(Variable::getName);
	private boolean m_concurrent = false;
	@Nullable private Duration m_timeout;
	
	public static ProgramOperationDescriptor load(File descFile, JsonMapper parser)
		throws StreamReadException, DatabindException, IOException {
		return parser.readValue(descFile, ProgramOperationDescriptor.class);
	}

	@JsonProperty("commandLine")
	public List<String> getCommandLine() {
		return m_commandLine;
	}

	@JsonProperty("commandLine")
	public void setCommandLine(List<String> cmdLine) {
		Preconditions.checkNotNull(cmdLine);
		
		m_commandLine = cmdLine;
	}

	@JsonProperty("workingDirectory")
	public File getWorkingDirectory() {
		return m_workingDirectory;
	}

	@JsonProperty("workingDirectory")
	public void setWorkingDirectory(File dir) {
		m_workingDirectory = dir;
	}
	
	@JsonProperty("operationSubmodel")
	public MDTSubmodelReference getOperationSubmodel() {
		return m_operationSubmodelRef;
	}

	@JsonProperty("operationSubmodel")
	public void setOperationSubmodel(MDTSubmodelReference ref) {
		m_operationSubmodelRef = ref;
	}

	@JsonProperty("inputVariables")
	public KeyedValueList<String,Variable> getInputVariables() {
		return m_inputVariables;
	}

	@JsonProperty("inputVariables")
	public void setInputVariables(List<Variable> variables) {
		m_inputVariables = KeyedValueList.from(variables, Variable::getName);
	}

	@JsonProperty("outputVariables")
	public KeyedValueList<String,Variable> getOutputVariables() {
		return m_outputVariables;
	}

	@JsonProperty("outputVariables")
	public void getOutputVariables(List<Variable> variables) {
		m_outputVariables = KeyedValueList.from(variables, Variable::getName);
	}

	@JsonProperty("concurrent")
	public boolean isConcurrent() {
		return m_concurrent;
	}

	@JsonProperty("concurrent")
	public void setConcurrent(boolean flag) {
		m_concurrent = flag;
	}

	@JsonProperty("timeout")
	public String getTimeoutString() {
		return FOption.map(m_timeout, Duration::toString);
	}
	public Duration getTimeout() {
		return m_timeout;
	}

	@JsonProperty("timeout")
	public void setTimeoutString(String durStr) {
		m_timeout = UnitUtils.parseDuration(durStr);
	}
	public void setTimeout(Duration to) {
		m_timeout = to;
	}
	
	@Override
	public String toString() {
		String smStr = ( m_operationSubmodelRef != null ) ? String.format(", submodel=%s", m_operationSubmodelRef) : "";
		String inVarNames = FStream.from(m_inputVariables).map(Variable::getName).join(",", "{", "}");
		String outVarNames = FStream.from(m_outputVariables).map(Variable::getName).join(",", "{", "}");
		String workingDirStr = FOption.mapOrElse(getWorkingDirectory(),
												f -> String.format(", working-dir=%s", f), "");
		return String.format("command=%s%s%s, inputs=%s, outputs=%s",
								m_commandLine, smStr, workingDirStr, inVarNames, outVarNames);
	}
}
