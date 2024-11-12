package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.UnitUtils;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.task.Parameter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public final class ProgramOperationDescriptor {
	private List<String> m_commandLine = Collections.emptyList();
	@Nullable private File m_workingDirectory;
	@Nullable private String m_submodelRef;
	private KeyedValueList<String,Parameter> m_inputParameters = KeyedValueList.newInstance(Parameter::getName);
	private KeyedValueList<String,Parameter> m_outputParameters = KeyedValueList.newInstance(Parameter::getName);
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
	
	@JsonProperty("submodel")
	public String getSubmodelReference() {
		return m_submodelRef;
	}

	@JsonProperty("submodel")
	public void setSubmodelReference(String ref) {
		m_submodelRef = ref;
	}

	@JsonProperty("inputParameters")
	public KeyedValueList<String,Parameter> getInputParameters() {
		return m_inputParameters;
	}

	@JsonProperty("inputParameters")
	public void setInputParameters(List<Parameter> parameters) {
		m_inputParameters = KeyedValueList.from(parameters, Parameter::getName);
	}

	@JsonProperty("outputParameters")
	public KeyedValueList<String,Parameter> getOutputParameters() {
		return m_outputParameters;
	}

	@JsonProperty("outputParameters")
	public void getOutputParameters(List<Parameter> parameters) {
		m_outputParameters = KeyedValueList.from(parameters, Parameter::getName);
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
		String smStr = ( m_submodelRef != null ) ? String.format(", submodel=%s", m_submodelRef) : "";
		String inVarNames = FStream.from(m_inputParameters).map(Parameter::getName).join(",", "{", "}");
		String outVarNames = FStream.from(m_outputParameters).map(Parameter::getName).join(",", "{", "}");
		String workingDirStr = FOption.mapOrElse(getWorkingDirectory(),
												f -> String.format(", working-dir=%s", f), "");
		return String.format("command=%s%s%s, inputs=%s, outputs=%s",
								m_commandLine, smStr, workingDirStr, inVarNames, outVarNames);
	}
}