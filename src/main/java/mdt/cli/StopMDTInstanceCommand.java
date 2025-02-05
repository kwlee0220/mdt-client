package mdt.cli;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.UnitUtils;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceStatus;
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
	private static final Duration POLL_INTERVAL = UnitUtils.parseDuration("1s");
	private static final Duration TIMEOUT = UnitUtils.parseDuration("30s");
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance id list to stop.")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="start all stopped MDTInstances")
	private boolean m_stopAll;
	
	@Option(names={"--recursive", "-r"}, description="start all dependent instances recursively")
	private boolean m_recursive;

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new StopMDTInstanceCommand(), args);
	}

	public StopMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();

		List<HttpMDTInstanceClient> targetInstList;
		if ( m_stopAll ) {
			targetInstList = client.getInstanceAllByFilter("instance.status = 'RUNNING'");
			
			// 전체를 stop 시키는 경우는 굳이 recursive하게 instance들을 stop 시킬 필요가 없다.
			m_recursive = false;
		}
		else {
			targetInstList = FStream.from(m_instanceIds)
									.map(client::getInstance)
									.toList();
		}
		
		for ( HttpMDTInstanceClient instance: targetInstList ) {
			try {
				stopInstance(instance);
			}
			catch ( Exception e ) {
				System.out.printf("failed to stop instance: %s, cause=%s%n", instance.getId(), e);
			}
		}
	}
	
	private void stopInstance(HttpMDTInstanceClient instance) throws TimeoutException, InterruptedException,
																		ExecutionException {
		if ( m_recursive ) {
			List<HttpMDTInstanceClient> components = instance.getAllComponents();
			for ( HttpMDTInstanceClient comp: components ) {
				stopInstance(comp);
			}
		}

		if ( m_verbose ) {
			System.out.printf("Stopping MDTInstance[%s]%n", instance.getId());
		}
		instance.stop(null, null);
		if ( !m_nowait ) {
			instance.waitWhileStatus(status -> status == MDTInstanceStatus.STOPPING, POLL_INTERVAL, TIMEOUT);
			if ( m_verbose ) {
				System.out.printf("Stopped MDTInstance[%s]%n", instance.getId());
			}
		}
	}
	
	private void waitAllInstanceStopped(List<HttpMDTInstanceClient> instList) throws InterruptedException {
		List<HttpMDTInstanceClient> remains = Lists.newArrayList(instList);
		while ( remains.size() > 0 ) {
			remains = FStream.from(remains)
							.peek(inst -> inst.reload())
							.filter(inst -> inst.getStatus() == MDTInstanceStatus.STOPPING)
							.toList();
		}
	}
}
