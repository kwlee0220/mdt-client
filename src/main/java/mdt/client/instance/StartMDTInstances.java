package mdt.client.instance;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.StopWatch;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.Executions;
import utils.async.Guard;
import utils.async.StartableExecution;
import utils.func.CheckedRunnableX;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Try;
import utils.stream.FStream;

import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTModelServiceOld;
import mdt.model.sm.info.CompositionItem;
import mdt.model.sm.info.TwinComposition;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class StartMDTInstances implements CheckedRunnableX<InterruptedException> {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstances.class);
	private static final Duration DEFAULT_POLLING_INTERVAL = UnitUtils.parseDuration("1s");
	private static final ExecutorService DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();

	private final MDTInstanceManager m_manager;
	private final List<String> m_instanceIdList;
	private final Duration m_pollingInterval;
	private final Duration m_timeout;
	private final boolean m_nowait;
	private final boolean m_startAll;
	private final ExecutorService m_executor;
	private final boolean m_recursive;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private int m_pendingCount = 0;	// 시작 대기중인 인스턴스 수
	@GuardedBy("m_guard") private int m_startingCount = 0;	// 시작 과정 중인 인스턴스 수
	// m_triedInstances: 이미 시작을 시도한 인스턴스 목록
	@GuardedBy("m_guard") private Set<String> m_triedInstances = Sets.newHashSet();
	
	private StartMDTInstances(@Nonnull Builder builder) {
		Preconditions.checkNotNull(builder != null, "Builder is not provided");
		Preconditions.checkNotNull(builder.m_manager, "MDTInstanceManager is not set");
		
		m_manager = builder.m_manager;
        m_instanceIdList = builder.m_instanceIdList;
        m_pollingInterval = FOption.getOrElse(builder.m_pollingInterval, DEFAULT_POLLING_INTERVAL);
        m_timeout = builder.m_timeout;
        m_nowait = builder.m_nowait;
        m_startAll = builder.m_startAll;
        m_executor = FOption.getOrElse(builder.m_executor, DEFAULT_EXECUTOR);
        m_recursive = builder.m_recursive;
	}
	
	@Override
	public void run() throws InterruptedException {
		// 시작시킬 MDTInstance 목록을 구성한다.
		List<? extends MDTInstance> initialInstances;
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
			for ( MDTInstance instance : initialInstances ) {
				if ( m_triedInstances.add(instance.getId()) ) {
					if ( s_logger.isDebugEnabled() ) {
						s_logger.debug("Submitting to start MDTInstance: id=" + instance.getId());
					}
					
					startInstanceAsync(instance);
				}
			}

			if ( !m_nowait || m_recursive ) {
				// 모든 인스턴스의 시작 작업이 완료될 때까지 대기한다.
				m_guard.awaitCondition(() -> m_startingCount <= 0 && m_pendingCount <= 0)
						.andReturn();
			}
			else {
				// 모든 대상 인스턴스에 대한 시작을 요청할 때까지 대기한다.
				// 시작 작업이 완료될 때까지 대기하지 않는다.
				m_guard.awaitCondition(() -> m_pendingCount <= 0)
						.andReturn();
				Thread.sleep(1000);
			}
		}
		finally {
			m_executor.shutdown();
		}
	}
	
	private Try<MDTInstance> getInstance(String id) {
		return Try.get(() -> m_manager.getInstance(id))
					.ifFailed(error -> {
						System.out.printf("Cannot find MDTInstance: id=%s, cause=%s%n", id, error);
					});
	}
	
	private StartableExecution<Void> startInstanceAsync(MDTInstance instance) {
		if ( s_logger.isDebugEnabled() ) {
			String instId = instance.getId();
			String toStr = FOption.mapOrElse(m_timeout, to -> to.toString(), "none");
            s_logger.debug("Submit MDTInstance: id={}, poll={}, timeout={} ...", instId, m_pollingInterval, toStr);
		}
	    
		StartableExecution<Void> exec = Executions.toExecution(() -> {
			startInstanceInOwnThread(instance);
		}, m_executor);
		exec.whenFailed(cause -> {
			System.out.printf("Failed to start MDTInstance: id=%s, cause=%s%n", instance.getId(), cause);
		});

		// 비동기적으로 시작시키고 m_pendingCount를 증가시킨다.
		// 이 작업을 쓰레드 풀이 부족한 경우 바로 시작하지 않고 대기할 수 있다.
		m_guard.run(() -> {
            ++m_pendingCount;
        });
		exec.start();
		
		return exec;
	}

	@SuppressWarnings("unused")
	private List<MDTInstance> listDependentInstanceAll(HttpMDTInstanceClient inst) {
		MDTModelServiceOld mdtInfo =  MDTModelServiceOld.of(inst);
		
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
													s_logger.error("Cannot find MDTInstance of AAS-ID={}: cause={}",
																		aasId, error);
												}))
						.toList();
		
	}
	
	private void startInstanceInOwnThread(MDTInstance instance)
		throws InvalidResourceStatusException, TimeoutException, InterruptedException, ExecutionException {
		StopWatch watch = StopWatch.start();

		m_guard.run(() -> {
			// 쓰레드를 할당받아 실제로 시작되기 때문에 m_pendingCount를 감소시키고
			// m_startingCount를 증가시킨다.
			// 즉, m_startingCount는 실제로 시작을 하고 있는 인스턴스 수를 나타낸다.
			--m_pendingCount;
			++m_startingCount;
		});
		
		try {
			try {
				s_logger.debug("Starting MDTInstance: id={}", instance.getId());
				
				instance.start(m_pollingInterval, m_timeout);
				s_logger.info("Started: MDTInstance: id={}", instance.getId());
			}
			catch ( InvalidResourceStatusException e ) {
				if ( instance.getStatus() == MDTInstanceStatus.RUNNING ) {
					if ( s_logger.isDebugEnabled() ) {
						s_logger.debug("Ignore already running instance: {}, elapsed={}s%n",
										instance.getId(), watch.stopAndGetElpasedTimeString());
					}
				}
				else {
					throw e;
				}
			}

			// 시작된 인스턴스의 종속 인스턴스들을 시작한다.
			List<MDTInstance> dependents = (m_recursive)
											? MDTModelServiceOld.of(instance).getSubComponentAll()
											: Collections.emptyList();
			if ( dependents.size() > 0 ) {
				if ( s_logger.isInfoEnabled() ) {
					s_logger.info("Starting dependent instances: " + Funcs.map(dependents, MDTInstance::getId));
				}
			}
			for ( MDTInstance dependent: dependents ) {
				if ( m_triedInstances.add(dependent.getId()) ) {
					startInstanceAsync(dependent);
				}
			}
		}
		catch ( InvalidResourceStatusException |  TimeoutException | InterruptedException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			s_logger.error("Failed: start MDTInstance: id={}, cause={}", instance.getId(), ""+cause);
			throw e;
		}
		finally {
			// 시작 작업이 완료되었기 때문에 m_startingCount를 감소시킨다.
			m_guard.run(() -> {
				--m_startingCount;
			});
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private MDTInstanceManager m_manager;
		private List<String> m_instanceIdList = null;
		private Duration m_pollingInterval = null;
		private Duration m_timeout = null;
		private boolean m_nowait = false;
		private boolean m_startAll = false;
		private ExecutorService m_executor = null;
		private boolean m_recursive = false;
		
		public StartMDTInstances build() {
			return new StartMDTInstances(this);
		}
		
		public Builder mdtInstanceManager(MDTInstanceManager manager) {
            m_manager = manager;
            return this;
		}
	
		public Builder instanceIdList(List<String> idList) {
			m_instanceIdList = idList;
			return this;
		}
		public Builder instanceIds(String... ids) {
			m_instanceIdList = Lists.newArrayList(ids);
			return this;
		}
		
		public Builder pollingInterval(Duration interval) {
			m_pollingInterval = interval;
			return this;
		}
		
		public Builder timeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		public Builder nowait(boolean flag) {
			m_nowait = flag;
			return this;
		}
		
		public Builder startAll(boolean flag) {
			m_startAll = flag;
			return this;
		}
		
		public Builder nthreads(int nthreads) {
			Preconditions.checkArgument(nthreads > 0, "invalid nthreads: " + nthreads);
            m_executor = Executors.newFixedThreadPool(nthreads);
            return this;
		}
		
		public Builder executor(ExecutorService executor) {
			m_executor = executor;
			return this;
		}
		
		public Builder recursive(boolean flag) {
			m_recursive = flag;
			return this;
		}
	}
}
