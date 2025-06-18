package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;

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
	private static final String DEFAULT_POLL_INTERVAL = "1s";

	@Option(names={"--operation"}, paramLabel="operation-ref",
			description="target operation element reference (<instance-id>:<submodel-idshort>:<element-idshort>)")
	public void setOperation(String refString) {
		m_operationRef = (DefaultElementReference)ElementReferences.parseExpr(refString);
	}
	private DefaultElementReference m_operationRef;

	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"1s\", \"500ms\"")
	private String m_pollInterval = DEFAULT_POLL_INTERVAL;
	
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	private String m_timeout = null;

	@Option(names={"--update", "-u"}, description="update Operation variables")
	private boolean m_updateOperation = false;

	@Option(names={"--showResult"}, description="show output/inoutput operation variables")
	private boolean m_showResult = false;
	
	public AASOperationTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		Instant started = Instant.now();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTask.class.getName());
		
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_OPERATION, m_operationRef);
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_POLL_INTERVAL, m_pollInterval);
		FOption.accept(m_timeout, to -> descriptor.addOrReplaceOption(AASOperationTask.OPTION_TIMEOUT, to));
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_UPDATE_OPVARS, ""+m_updateOperation);
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_SHOW_RESULT, ""+m_showResult);
		
		m_operationRef.activate(manager);
		TaskDescriptors.loadVariablesFromOperation(m_operationRef, descriptor);

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromArguments(manager, descriptor);
		
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
