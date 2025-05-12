package mdt.cli;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;

import mdt.client.instance.StopMDTInstances;
import mdt.model.MDTManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "stop",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Stop a running MDT instance."
)
public class StopMDTInstanceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstanceCommand.class);
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance id list to stop.")
	private List<String> m_instanceIds;

	private Duration m_pollingInterval = UnitUtils.parseDuration("1s");
	@Option(names={"--poll"}, paramLabel="duration",
			description="Status polling interval (e.g. \"1s\", \"500ms\"")
	private void setPollingInterval(String intervalStr) {
		m_pollingInterval = UnitUtils.parseDuration(intervalStr);
	}

	private Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration",
			description="Status sampling timeout (e.g. \"30s\", \"1m\"). default: null")
	private void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	@Option(names={"--all", "-a"}, description="start all stopped MDTInstances")
	private boolean m_stopAll;

	@Option(names={"--nthreads", "-n"}, defaultValue = "1", description="Thread pool size (default: 1)")
	private int m_nthreads = 1;
	
	@Option(names={"--recursive", "-r"}, description="start all dependent instances recursively")
	private boolean m_recursive;

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;

	public static final void main(String... args) throws Exception {
		main(new StopMDTInstanceCommand(), args);
	}

	public StopMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		StopMDTInstances.builder()
						.mdtInstanceManager(mdt.getInstanceManager())
						.instanceIdList(m_instanceIds)
						.pollingInterval(m_pollingInterval)
						.timeout(m_timeout)
						.nowait(m_nowait)
						.stopAll(m_stopAll)
						.nthreads(m_nthreads)
						.recursive(m_recursive)
						.build()
						.run();
	}
}
