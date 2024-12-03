package mdt.cli;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import utils.StopWatch;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.Executions;
import utils.async.Guard;
import utils.async.StartableExecution;
import utils.func.FOption;
import utils.func.Try;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.sm.info.ComponentItem;
import mdt.model.sm.info.TwinComposition;
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
public class StartMDTInstanceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstanceCommand.class);

	@Parameters(index="0..*", paramLabel="id", description="MDTInstance id to start.")
	private List<String> m_instanceIdList;

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

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;
	
	@Option(names={"--all", "-a"}, description="start all non-running MDTInstances")
	private boolean m_startAll;

	private ExecutorService m_executor = Executors.newSingleThreadExecutor();
	@Option(names={"--nthreads", "-n"}, defaultValue = "1", description="Thread pool size (default: 1)")
	private void setThreadCount(int count) {
		Preconditions.checkArgument(count >= 1);
		m_executor = Executors.newFixedThreadPool(count);
	}
	
	@Option(names={"--recursive", "-r"}, description="start all dependent instances recursively")
	private boolean m_recursive;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	private HttpMDTInstanceManagerClient m_manager;
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private int m_pendingCount = 0;
	@GuardedBy("m_guard") private Set<String> m_runningInstanceIds = Sets.newHashSet();

	public static final void main(String... args) throws Exception {
		main(new StartMDTInstanceCommand(), args);
	}

	public StartMDTInstanceCommand() {
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTManager mdt) throws Exception {
		m_manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		List<HttpMDTInstanceClient> initialInstances;
		if ( m_startAll ) {
			initialInstances = m_manager.getAllInstancesByFilter("instance.status != 'RUNNING'");
		}
		else {
			initialInstances = FStream.from(m_instanceIdList)
									.flatMapTry(this::getInstance)
									.toList();
		}
		
		try {
			m_guard.run(() -> m_pendingCount += initialInstances.size());
			for ( HttpMDTInstanceClient instance: initialInstances ) {
				startInstanceAsync(instance);
			}
			
			m_guard.awaitWhile(() -> m_pendingCount > 0);
		}
		finally {
			m_executor.shutdown();
		}
	}
	
	private Try<HttpMDTInstanceClient> getInstance(String id) {
		return Try.get(() -> m_manager.getInstance(id))
					.ifFailed(error -> {
						System.out.printf("Cannot find MDTInstance of id=%s, cause=%s%n",
											id, error);
					});
	}
	
	private StartableExecution<Void> startInstanceAsync(HttpMDTInstanceClient instance) {
		if ( m_verbose ) {
			String instId = instance.getId();
			String toStr = FOption.mapOrElse(m_timeout, to -> to.toString(), "none");
			System.out.printf("Starting MDTInstance %s, poll=%s, timeout=%s ...%n",
								instId, m_pollingInterval, toStr);
		}
		
		return Executions.runAsync(() -> startInstance(instance), m_executor);
	}
	
	private void startInstance(HttpMDTInstanceClient instance)
		throws InvalidResourceStatusException, TimeoutException, InterruptedException {
		StopWatch watch = StopWatch.start();
		
		String instId = instance.getId();
		try {
			if ( !m_runningInstanceIds.contains(instId) ) {
				instance.start(m_pollingInterval, m_timeout);
				if ( m_verbose ) {
					System.out.printf("Started: %s, elapsed=%ss%n", instId, watch.stopAndGetElpasedTimeString());
				}
			}
		}
		catch ( Throwable e ) {
			if ( m_verbose ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				System.out.printf("Failed: start MDTInstance: id=%s, cause=%s%n", instId, cause);
			}
			
			throw e;
		}
		finally {
			if ( instance.getStatus() == MDTInstanceStatus.RUNNING ) {
				List<HttpMDTInstanceClient> dependents = (m_recursive)
														? instance.getAllComponents()
														: Collections.emptyList();
				m_guard.runAndSignalAll(() -> {
					m_runningInstanceIds.add(instance.getId());
					m_pendingCount += dependents.size();
					--m_pendingCount;
				});
				for ( HttpMDTInstanceClient dependent: dependents ) {
					if ( !m_runningInstanceIds.contains(dependent.getId()) ) {
						startInstanceAsync(dependent).start();
					}
				}
			}
			else {
				m_guard.runAndSignalAll(() -> --m_pendingCount);
			}
		}
	}
	
	private List<HttpMDTInstanceClient> listDependentInstanceAll(HttpMDTInstanceClient inst) {
		TwinComposition tcomp = inst.getInformationModel().getTwinComposition();
		String parent = tcomp.getCompositionID();
		
		Map<String,ComponentItem> itemMap = FStream.from(tcomp.getComponentItems())
													.toMap(item -> item.getID());
		List<String> childAasIdList = FStream.from(tcomp.getCompositionDependencies())
											.filter(dep -> dep.getDependencyType().equals("contain"))
											.filter(dep -> dep.getSource().equals(parent))
											.flatMapNullable(dep -> itemMap.get(dep.getTarget()))
											.map(ComponentItem::getReference)
											.toList();
		return FStream.from(childAasIdList)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId))
												.ifFailed(error -> {
													System.out.printf("Cannot find MDTInstance of AAS-ID=%s, cause=%s%n",
																		aasId, error);
												}))
						.toList();
		
	}
}
