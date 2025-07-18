package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.func.FOption;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
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
public class HttpTaskCommand extends AbstractTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);
	private static final String DEFAULT_POLL_INTERVAL = "1s";

	@Option(names={"--endpoint"}, paramLabel="endpoint",
			description="The endpoint for the HTTP-based MDTOperationServer.")
	private String m_endpoint = null;

	@Option(names={"--opId"}, paramLabel="operation-id",
			description="target operation-id (<instance-id>/<submodel-idshort>")
	private String m_opId;

	@Option(names={"--poll"}, paramLabel="duration", defaultValue=DEFAULT_POLL_INTERVAL,
			description="Status polling interval (e.g. default=" + DEFAULT_POLL_INTERVAL + ")")
	private String m_pollInterval;
	
	public HttpTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(HttpTask.class.getName());
		loadTaskDescriptor(descriptor, manager);
		
		FOption.accept(m_endpoint, ep -> descriptor.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, ep));
		Preconditions.checkArgument(descriptor.findStringOption(HttpTask.OPTION_SERVER_ENDPOINT).isPresent(),
				                    "HTTP server endpoint is not specified: use '--endpoint' option");
		
		FOption.accept(m_opId, oid -> descriptor.addOrReplaceOption(HttpTask.OPTION_OPERATION, oid));
		Preconditions.checkArgument(descriptor.findStringOption(HttpTask.OPTION_OPERATION).isPresent(),
                                                    "Operation ID is not specified: use '--opId' option");
		
		if ( m_pollInterval != null ) {
			descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, m_pollInterval);
		}
		else {
			descriptor.findStringOption(HttpTask.OPTION_POLL_INTERVAL)
						.ifAbsent(() -> descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL,
																		DEFAULT_POLL_INTERVAL));
		}
		
		HttpTask httpTask = new HttpTask(descriptor);
		httpTask.run(mdt.getInstanceManager());
		
		Duration elapsed = Duration.between(started, Instant.now());
		getLogger().info("HttpTask: elapsedTime={}", elapsed);
	}

	public static void main(String... args) throws Exception {
		main(new HttpTaskCommand(), args);
		System.exit(0);
	}
}
