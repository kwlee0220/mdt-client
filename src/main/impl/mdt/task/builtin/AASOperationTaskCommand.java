package mdt.task.builtin;

import java.time.Duration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.KeyedValueList;
import utils.UnitUtils;

import mdt.model.sm.DefaultSubmodelElementReference;
import mdt.task.MultiParameterTaskCommand;
import mdt.task.Parameter;
import mdt.task.TaskException;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTaskCommand extends MultiParameterTaskCommand<AASOperationTask> {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTaskCommand.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	private DefaultSubmodelElementReference m_operationRef;
	@Option(names={"--operation"}, paramLabel="reference", required = true,
			description="the mdt-reference to the target operation submodel")
	public void setOperation(String refString) {
		m_operationRef = DefaultSubmodelElementReference.parseString(refString);
	}

	@Option(names={"--async"}, description="invoke asynchronously")
	private boolean m_async = false;

	private Duration m_poll = DEFAULT_POLL_INTERVAL;
	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"1s\", \"500ms\"")
	public void setPollInterval(String intvStr) {
		m_poll = UnitUtils.parseDuration(intvStr);
	}
	
	public AASOperationTaskCommand() {
		setLogger(s_logger);
	}
	
	@Override
	protected AASOperationTask newTask(KeyedValueList<String,Parameter> parameters,
										Set<String> outputParameterNames) throws TaskException {
		return new AASOperationTask(m_operationRef, m_async, m_poll, m_timeout, parameters, outputParameterNames);
	}
	
	public static void main(String... args) throws Exception {
		main(new AASOperationTaskCommand(), args);
	}
}
