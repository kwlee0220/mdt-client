package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Tuple;
import utils.UnitUtils;
import utils.Utilities;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.workflow.model.BooleanOption;
import mdt.workflow.model.DurationOption;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "http",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Http-based task execution command."
)
public class HttpTaskCommand extends MultiVariablesCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);

	@Option(names={"--server"}, paramLabel="endpoint", defaultValue="http://localhost:12987", 
			description="The endpoint for the HTTP-based MDTOperationServer. (default: http://localhost:12987)")
	private String m_opServerEndpoint;

	@Option(names={"--opid"}, paramLabel="operation-id", required=true,
			description="target operation-id (<instance-id>/<submodel-idshort>")
	private String m_opId;

	private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
	@Option(names={"--poll"}, paramLabel="duration", defaultValue="3s",
			description="Status polling interval (e.g. \"5s\", \"500ms\"")
	public void setPollInterval(String intervalStr) {
		m_pollInterval = UnitUtils.parseDuration(intervalStr);
	}
	
	private Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}

	@Option(names={"--sync"}, defaultValue="false", description="invoke synchronously")
	private boolean m_sync = false;
	
	public HttpTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(HttpTask.class.getName());

		Tuple<String,String> tup = Utilities.split(m_opId, '/');
		ByIdShortSubmodelReference opSubmodelRef = ByIdShortSubmodelReference.ofIdShort(tup._1, tup._2);
		opSubmodelRef.activate(manager);
		
		descriptor.addOption(HttpTask.OPTION_OPERATION, m_opId);
		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, m_opServerEndpoint);
		descriptor.getOptions().add(new DurationOption(HttpTask.OPTION_POLL_INTERVAL, m_pollInterval));
		if ( m_timeout != null ) {
			descriptor.getOptions().add(new DurationOption(HttpTask.OPTION_TIMEOUT, m_timeout));
		}
		descriptor.getOptions().add(new BooleanOption(HttpTask.OPTION_SYNC, m_sync));

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromParameters(manager, descriptor);
		
		HttpTask httpTask = new HttpTask(descriptor);
		httpTask.run(mdt.getInstanceManager());
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("HttpTask: elapsedTime={}", elapsed);
		}
	}

	public static void main(String... args) throws Exception {
		main(new HttpTaskCommand(), args);
		System.exit(0);
	}
}
