package mdt.cli;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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
import utils.func.Funcs;
import utils.func.Try;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTModelService;
import mdt.model.sm.info.CompositionItem;
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
public class StartMDTInstanceCommand2 extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstanceCommand2.class);

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
	
	private HttpMDTInstanceManager m_manager;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private int m_pendingCount = 0;	// 시작 대기중인 인스턴스 수
	@GuardedBy("m_guard") private int m_startingCount = 0;	// 시작 과정 중인 인스턴스 수
	// m_triedInstances: 이미 시작을 시도한 인스턴스 목록
	@GuardedBy("m_guard") private Set<String> m_triedInstances = Sets.newHashSet();

	public static final void main(String... args) throws Exception {
		main(new StartMDTInstanceCommand2(), args);
	}

	public StartMDTInstanceCommand2() {
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTManager mdt) throws Exception {
		m_manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		// 시작시킬 MDTInstance 목록을 구성한다.
		List<HttpMDTInstance> initialInstances;
		if ( m_startAll ) {
			initialInstances = FStream.from(m_manager.getInstanceAll())
										.filter(inst -> inst.getStatus() != MDTInstanceStatus.RUNNING)
										.toList();
		}
		else {
			initialInstances = FStream.from(m_instanceIdList)
									.flatMapTry(this::getInstance)
									.toList();
		}
		
		try {
			for ( HttpMDTInstance instance: initialInstances ) {
				if ( m_triedInstances.add(instance.getId()) ) {
					startInstanceAsync(instance);
				}
			}
			
			if ( !m_nowait || m_recursive ) {
				m_guard.awaitCondition(() -> m_startingCount <= 0 && m_pendingCount <= 0).andReturn();
			}
			else {
				m_guard.awaitCondition(() -> m_pendingCount <= 0).andReturn();
				Thread.sleep(1000);
			}
		}
		finally {
			m_executor.shutdown();
		}
	}
	
	private Try<HttpMDTInstance> getInstance(String id) {
		return Try.get(() -> m_manager.getInstance(id))
					.ifFailed(error -> {
						System.out.printf("Cannot find MDTInstance of id=%s, cause=%s%n", id, error);
					});
	}
	
	private StartableExecution<Void> startInstanceAsync(MDTInstance instance) {
		if ( m_verbose ) {
			String instId = instance.getId();
			String toStr = FOption.mapOrElse(m_timeout, to -> to.toString(), "none");
			System.out.printf("Submit %s, poll=%s, timeout=%s ...%n",
								instId, m_pollingInterval, toStr);
		}

		// 비동기적으로 시작시키고 m_pendingCount를 증가시킨다.
		// 이 작업을 쓰레드 풀이 부족한 경우 바로 시작하지 않고 대기할 수 있다.
		m_guard.run(() -> {
            ++m_pendingCount;
        });
	    
		StartableExecution<Void> exec = Executions.toExecution(() -> {
			startInstanceInOwnThread(instance);
		}, m_executor);
		exec.start();
		
		return exec;
	}
	
	private void startInstanceInOwnThread(MDTInstance instance)
		throws InvalidResourceStatusException, TimeoutException, InterruptedException, ExecutionException {
		StopWatch watch = StopWatch.start();
		
		if ( m_verbose ) {
			System.out.printf("Starting: %s%n", instance.getId());
		}

		m_guard.run(() -> {
			// 쓰레드를 할당받아 실제로 시작되기 때문에 m_pendingCount를 감소시키고
			// m_startingCount를 증가시킨다.
			// 즉, m_startingCount는 실제로 시작을 하고 있는 인스턴스 수를 나타낸다.
			--m_pendingCount;
			++m_startingCount;
		});
		
		try {
			try {
				instance.start(m_pollingInterval, m_timeout);
			}
			catch ( InvalidResourceStatusException e ) {
				if ( e instanceof InvalidResourceStatusException ) {
					if ( instance.getStatus() != MDTInstanceStatus.RUNNING ) {
						if ( m_verbose ) {
							System.out.printf("Ignore already running instance: %s, elapsed=%ss%n",
												instance.getId(), watch.stopAndGetElpasedTimeString());
						}
					}
				}
				else {
					throw e;
				}
			}

			// 시작된 인스턴스의 종속 인스턴스들을 시작한다.
			List<MDTInstance> dependents = (m_recursive)
											? MDTModelService.of(instance).getSubComponentAll()
											: Collections.emptyList();
			if ( dependents.size() > 0 && m_verbose ) {
				System.out.println("Starting dependent instances: " + Funcs.map(dependents, MDTInstance::getId));
			}
			for ( MDTInstance dependent: dependents ) {
				if ( m_triedInstances.add(dependent.getId()) ) {
					startInstanceAsync(dependent);
				}
			}
		}
		catch ( InvalidResourceStatusException |  TimeoutException | InterruptedException e ) {
			if ( m_verbose ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				System.out.printf("Failed: start MDTInstance: id=%s, cause=%s%n", instance.getId(), cause);
			}
			throw e;
		}
		finally {
			// 시작 작업이 완료되었기 때문에 m_startingCount를 감소시킨다.
			m_guard.run(() -> {
				--m_startingCount;
			});
		}
	}
	
	@SuppressWarnings("unused")
	private List<HttpMDTInstance> listDependentInstanceAll(HttpMDTInstance inst) {
		MDTModelService mdtInfo =  MDTModelService.of(inst);
		
		TwinComposition tcomp = mdtInfo.getInformationModel().getTwinComposition();
		String parent = tcomp.getCompositionID();
		
		Map<String,CompositionItem> itemMap = FStream.from(tcomp.getCompositionItems())
													.tagKey(item -> item.getID())
													.toMap();
		List<String> childAasIdList = FStream.from(tcomp.getCompositionDependencies())
											.filter(dep -> dep.getDependencyType().equals("contain"))
											.filter(dep -> dep.getSourceId().equals(parent))
											.flatMapNullable(dep -> itemMap.get(dep.getTargetId()))
											.map(CompositionItem::getReference)
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
