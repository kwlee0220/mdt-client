package mdt.cli.instance;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.stream.FStream;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;
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
public class StopMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstanceCommand.class);
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance id list to stop")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="start all stopped MDTInstances")
	private boolean m_stopAll;

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

		List<MDTInstance> targetInstList;
		if ( m_stopAll ) {
			targetInstList = client.getAllInstancesByFilter("instance.status = 'RUNNING'");
		}
		else {
			targetInstList = FStream.from(m_instanceIds)
									.map(client::getInstance)
									.cast(MDTInstance.class)
									.toList();
		}
		for ( MDTInstance instance: targetInstList ) {
			try {
				System.out.println("stopping instance: " + instance.getId());
				stopInstance(instance);
			}
			catch ( Exception e ) {
				System.out.printf("failed to stop instance: %s, cause=%s%n", instance.getId(), e);
			}
		}
	}
	
	private void stopInstance(MDTInstance instance) throws TimeoutException, InterruptedException,
																		ExecutionException {
		instance.stop(null, null);
		if ( !m_nowait ) {
			// wait하는 경우에는 MDTInstance의 상태를 계속적으로 polling하여
			// 'STOPPING' 상태에서 벗어날 때까지 대기한다.
			Predicate<MDTInstanceStatus> whileStarting = status -> {
				if ( m_verbose ) {
					System.out.print(".");
				}
				return status == MDTInstanceStatus.STOPPING;
			};
			
			HttpMDTInstanceClient instClient = (HttpMDTInstanceClient)instance;
			instClient.waitWhileStatus(whileStarting, UnitUtils.parseDuration("1s"),
										UnitUtils.parseDuration("1m"));
			if ( m_verbose ) {
				System.out.println();
			}
		}
	}
}
