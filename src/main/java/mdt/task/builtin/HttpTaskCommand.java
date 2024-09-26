package mdt.task.builtin;

import java.time.Duration;

import utils.UnitUtils;

import mdt.task.MDTTaskCommand;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTaskCommand extends MDTTaskCommand<HttpTask> {
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);

	@Option(names={"--url"}, paramLabel="server-url", required=true,
			description="The endpoint for the HTTP-based task")
	private String m_taskServerEndpoint;

	private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
	@Option(names={"--poll"}, paramLabel="duration",
			description="Status polling interval (e.g. \"5s\", \"500ms\"")
	public void setPollInterval(String intervalStr) {
		m_pollInterval = UnitUtils.parseDuration(intervalStr);
	}
	
	@Override
	protected HttpTask newTask() {
		HttpTask task = new HttpTask();
		task.setPollInterval(m_pollInterval);
		task.setTaskServerEndpoint(m_taskServerEndpoint);
		
		return task;
	}

	public static void main(String... args) throws Exception {
		main(new HttpTaskCommand(), args);
		System.exit(0);
	}
}
