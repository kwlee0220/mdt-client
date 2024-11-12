package mdt.cli;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.UnitUtils;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstanceStatus;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "start",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Start an MDTInstance."
)
public class StartMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstanceCommand.class);
	
	@Parameters(index="0..*", paramLabel="id", description="MDTInstance id to start")
	private List<String> m_instanceIdList;

	@Option(names={"--poll"}, paramLabel="duration",
			description="Status polling interval (e.g. \"1s\", \"500ms\"")
	private String m_pollingInterval = "1s";

	@Option(names={"--timeout"}, paramLabel="duration",
			description="Status sampling timeout (e.g. \"30s\", \"1m\"")
	private String m_timeout = "1m";

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	@Option(names={"-vv"}, description="verbose")
	private boolean m_vverbose = false;

	public static final void main(String... args) throws Exception {
		main(new StartMDTInstanceCommand(), args);
	}

	public StartMDTInstanceCommand() {
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		List<HttpMDTInstanceClient> instList = startInstances(manager);
		if ( !m_nowait ) {
			for ( HttpMDTInstanceClient inst: instList ) {
				waitWhileStarting(inst);
			}
		}
		
		System.exit(0);
	}
	
	private List<HttpMDTInstanceClient> startInstances(HttpMDTInstanceManagerClient manager)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException {
		List<HttpMDTInstanceClient> instances = Lists.newArrayList();
		for ( String instId: m_instanceIdList ) {
			HttpMDTInstanceClient instance = manager.getInstance(instId);
			try {
				instance.start(null, null);
			}
			catch ( InvalidResourceStatusException e ) {
				System.out.printf("Failed to start instance: id=%s, cause=%s%n", instId, e);
			}
			
			if ( m_verbose  ) {
				System.out.printf("starting instance: %s ", instance.getId());
			}
			else if (m_vverbose ) {
				System.out.printf("starting instance: %s%n", instance.getId());
			}
			instances.add(instance);
			
		}
		
		return instances;
	}
	
	private void waitWhileStarting(HttpMDTInstanceClient instance) throws TimeoutException, InterruptedException,
																			ExecutionException {
		// wait하는 경우에는 MDTInstance의 상태를 계속적으로 polling하여
		// 'STARTING' 상태에서 벗어날 때까지 대기한다.
		Predicate<MDTInstanceStatus> whileStarting = status -> {
			if ( m_vverbose ) {
				System.out.printf("checking status: instance=%s status=%s%n",
									instance.getId(), status);
			}
			else if ( m_verbose ) {
				System.out.print(".");
			}
			return status == MDTInstanceStatus.STARTING;
		};
		instance.waitWhileStatus(whileStarting, UnitUtils.parseDuration(m_pollingInterval),
									UnitUtils.parseDuration(m_timeout));
		
		if ( m_verbose || m_vverbose ) {
			MDTInstanceStatus status = instance.getStatus();
			String svcEp = instance.getEndpoint();
			
			if ( m_verbose ) {
				System.out.println();
			}
			System.out.printf("instance: id=%s, status=%s, endpoint=%s%n", instance.getId(), status, svcEp);
		}
	}
}
