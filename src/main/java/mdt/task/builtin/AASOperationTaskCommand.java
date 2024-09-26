package mdt.task.builtin;

import java.time.Duration;

import utils.UnitUtils;

import mdt.task.MDTTaskCommand;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "aas", description = "AAS Operation-based task.")
public class AASOperationTaskCommand extends MDTTaskCommand<AASOperationTask> {
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	@Option(names={"--operation"}, paramLabel="reference", required = true,
			description="the mdt-reference to the target operation")
	private String m_opRefString;

	@Option(names={"--async"}, description="invoke asynchronously")
	private boolean m_async = false;

	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;
	@Option(names={"--pollInterval"}, paramLabel="duration",
			description="Status polling interval (e.g. \"1s\", \"500ms\"")
	public void setPollInterval(String intvStr) {
		m_pollInterval = UnitUtils.parseDuration(intvStr);
	}
	
	@Override
	protected AASOperationTask newTask() {
		AASOperationTask task = new AASOperationTask();
		task.setOperationReferenceString(m_opRefString);
		task.setPollInterval(m_pollInterval);
		task.setAsync(m_async);
		
		return task;
	}
	
	public static void main(String... args) throws Exception {
		main(new AASOperationTaskCommand(), args);
	}
}
