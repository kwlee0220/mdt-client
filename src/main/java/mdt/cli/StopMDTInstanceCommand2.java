package mdt.cli;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTModelService;
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
public class StopMDTInstanceCommand2 extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstanceCommand2.class);
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
		main(new StopMDTInstanceCommand2(), args);
	}

	public StopMDTInstanceCommand2() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();

		List<HttpMDTInstance> targetInstList;
		if ( m_stopAll ) {
			targetInstList = FStream.from(manager.getInstanceAll())
									.filter(inst -> inst.getStatus() == MDTInstanceStatus.RUNNING)
									.toList();
			
			// 전체를 stop 시키는 경우는 굳이 recursive하게 instance들을 stop 시킬 필요가 없다.
			m_recursive = false;
		}
		else {
			targetInstList = FStream.from(m_instanceIds)
									.map(manager::getInstance)
									.toList();
		}
		
		for ( HttpMDTInstance instance: targetInstList ) {
			try {
				stopInstance(instance);
			}
			catch ( Exception e ) {
				System.out.printf("failed to stop instance: %s, cause=%s%n", instance.getId(), e);
			}
		}
	}
	
	private void stopInstance(MDTInstance instance) throws TimeoutException, InterruptedException,
																		ExecutionException {
		if ( m_recursive ) {
			List<MDTInstance> components = MDTModelService.of(instance).getSubComponentAll();
			for ( MDTInstance comp: components ) {
				stopInstance(comp);
			}
		}

		if ( m_verbose ) {
			System.out.printf("Stopping MDTInstance[%s]%n", instance.getId());
		}
		instance.stop(null, null);
		if ( !m_nowait ) {
			HttpMDTInstance httpInstance = (HttpMDTInstance)instance;
			httpInstance.waitWhileStatus(status -> status == MDTInstanceStatus.STOPPING, POLL_INTERVAL, TIMEOUT);
			if ( m_verbose ) {
				System.out.printf("Stopped MDTInstance[%s]%n", instance.getId());
			}
		}
	}
}
