package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTaskCommand extends MDTCommand {
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
	
	private Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	public HttpTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		HttpTask httpTask = new HttpTask(m_mdtOpServerEndpoint, m_opId, m_pollInterval, m_sync, m_timeout);
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
