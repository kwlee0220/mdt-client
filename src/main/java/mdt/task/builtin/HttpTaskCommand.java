package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.MDTManager;
import mdt.task.MultiParameterTaskCommand;
import mdt.task.Parameter;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTaskCommand extends MultiParameterTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);

	@Option(names={"--server"}, paramLabel="server-endpoint", required=true,
			description="The endpoint for the HTTP-based MDTOperationServer")
	private String m_mdtOpServerEndpoint;
	
	@Option(names={"--id"}, paramLabel="op-id", required=true, description="Target operation id to call")
	private String m_opId;

	private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"5s\", \"500ms\"")
	public void setPollInterval(String intervalStr) {
		m_pollInterval = UnitUtils.parseDuration(intervalStr);
	}

	@Option(names={"--sync"}, description="invoke synchronously")
	private boolean m_sync = false;
	
	public HttpTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		HttpTask.Builder builder = HttpTask.builder()
											.serverEndpoint(m_mdtOpServerEndpoint)
											.operationId(m_opId)
											.pollInterval(m_pollInterval)
											.timeout(m_timeout)
											.sync(m_sync);
		Tuple<List<Parameter>, List<Parameter>> paramsPair = loadParameters();
		FStream.from(paramsPair._1).forEach(builder::addInputParameter);
		FStream.from(paramsPair._2).forEach(builder::addOutputParameter);
		
		HttpTask httpTask = builder.build();
		
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
