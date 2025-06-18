package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.Tuple;
import utils.Utilities;
import utils.func.FOption;

import mdt.model.MDTManager;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;

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
	private static final String DEFAULT_POLL_INTERVAL = "1s";

	@Option(names={"--submodel"}, paramLabel="submodel reference",
			description="Target submodel reference (e.g. <instance-id>/<submodel-idshort>)")
	private String m_opSmRefExpr;

	@Option(names={"--server"}, paramLabel="endpoint",
			description="The endpoint for the HTTP-based MDTOperationServer.")
	private String m_opServerEndpoint = null;

	@Option(names={"--opId"}, paramLabel="operation-id",
			description="target operation-id (<instance-id>/<submodel-idshort>")
	private String m_opId;

	@Option(names={"--poll"}, paramLabel="duration", defaultValue=DEFAULT_POLL_INTERVAL,
			description="Status polling interval (e.g. default=" + DEFAULT_POLL_INTERVAL + ")")
	private String m_pollInterval;
	
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	private String m_timeout = null;

	@Option(names={"--sync"}, defaultValue="false", description="invoke synchronously")
	private boolean m_sync = false;
	
	public HttpTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		TaskDescriptor descriptor;
		if ( m_opSmRefExpr != null ) {
            // Submodel reference가 지정된 경우, 해당 SubmodelReference에 대한 Task descriptor를 생성한다.
			DefaultSubmodelReference smRef = MDTExprParser.parseSubmodelReference(m_opSmRefExpr).evaluate();
            descriptor = TaskDescriptors.from(smRef);
            descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, m_opSmRefExpr);
        }
        else {
            // Submodel reference가 지정되지 않은 경우, 단순한 Task descriptor를 생성한다.
            descriptor = new TaskDescriptor();
        }
		descriptor.setType(HttpTask.class.getName());

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromArguments(manager, descriptor);
		
		FOption.accept(m_opServerEndpoint, ep -> descriptor.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, ep));
		Preconditions.checkArgument(descriptor.findStringOption(HttpTask.OPTION_SERVER_ENDPOINT).isPresent(),
				                    "HTTP server endpoint is not specified: use '--server' option");
		
		FOption.accept(m_opId, oid -> descriptor.addOrReplaceOption(HttpTask.OPTION_OPERATION, oid));
		Preconditions.checkArgument(descriptor.findStringOption(HttpTask.OPTION_OPERATION).isPresent(),
                                                    "Operation ID is not specified: use '--opId' option");
		
		if ( m_pollInterval != null ) {
			descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, m_pollInterval);
		}
		else {
			descriptor.findStringOption(HttpTask.OPTION_POLL_INTERVAL)
						.ifAbsent(() -> descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, DEFAULT_POLL_INTERVAL));
		}
		
		FOption.accept(m_timeout, to -> descriptor.addOrReplaceOption(HttpTask.OPTION_TIMEOUT, to));
		descriptor.addOrReplaceOption(HttpTask.OPTION_SYNC, ""+m_sync);
		
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
