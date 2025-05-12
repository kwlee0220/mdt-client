package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "aas",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "AAS Operation task execution command."
)
public class AASOperationTaskCommand extends MultiVariablesCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTaskCommand.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	@Option(names={"--operation"}, paramLabel="operation-ref",
			description="target operation element reference (<instance-id>:<submodel-idshort>:<element-idshort>)")
	public void setOperation(String refString) {
		m_operationRef = ElementReferences.parseExpr(refString);
	}
	private MDTElementReference m_operationRef;

	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;
	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"1s\", \"500ms\"")
	public void setPollInterval(String intvStr) {
		m_pollInterval = UnitUtils.parseDuration(intvStr);
	}
	
	private Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}

	@Option(names={"--update", "-u"}, description="update Operation variables")
	private boolean m_updateOperation = false;
	
	public AASOperationTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		Instant started = Instant.now();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		
		descriptor.addOption(AASOperationTask.OPTION_OPERATION, m_operationRef);
		descriptor.addOption(AASOperationTask.OPTION_POLL_INTERVAL, m_pollInterval);
		if ( m_timeout != null ) {
			descriptor.addOption(AASOperationTask.OPTION_TIMEOUT, m_timeout);
		}
		descriptor.addOption(AASOperationTask.OPTION_UPDATE_OPVARS, m_updateOperation);

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromParameters(manager, descriptor);
		
		AASOperationTask aasOpTask = new AASOperationTask(descriptor);
		aasOpTask.run(mdt.getInstanceManager());
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("AASOperationTask: ref={}, elapsedTime={}", m_operationRef, elapsed);
		}
	}
	
	public static void main(String... args) throws Exception {
		main(new AASOperationTaskCommand(), args);
	}
}
