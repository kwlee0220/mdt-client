package mdt.client.instance;

import java.time.Duration;
import java.util.List;
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
import utils.func.Optionals;
import utils.stream.FStream;

import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;

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
        m_pollingInterval = Optionals.getOrElse(builder.m_pollingInterval, DEFAULT_POLLING_INTERVAL);
        m_timeout = builder.m_timeout;
        m_nowait = builder.m_nowait;
        m_startAll = builder.m_startAll;
        m_executor = Optionals.getOrElse(builder.m_executor, DEFAULT_EXECUTOR);
        m_recursive = builder.m_recursive;
	}
	
	@Override
	public void run() throws InterruptedException {
		// 시작시킬 MDTInstance 목록을 구성한다.
		List<MDTInstance> initialInstances;
		if ( m_startAll ) {
			initialInstances = FStream.from(m_manager.getInstanceAllByFilter("instance.status != 'RUNNING'"))
										.cast(MDTInstance.class)
										.toList();
		}
		else {
			initialInstances = Lists.newArrayList();
			for ( String id: m_instanceIdList ) {
				try {
					initialInstances.add(m_manager.getInstance(id));
				}
				catch ( Exception e ) {
					System.out.printf("Failed to get MDTInstance: id=%s, cause=%s%n", id, e);
				}
			}
		}

		try {
			for ( MDTInstance instance : initialInstances ) {
				if ( m_triedInstances.add(instance.getId()) ) {
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
	
	private StartableExecution<Void> startInstanceAsync(MDTInstance instance) {
		if ( s_logger.isDebugEnabled() ) {
			String instId = instance.getId();
			String toStr = FOption.mapOrElse(m_timeout, to -> to.toString(), "none");
            s_logger.debug("Submitting to start MDTInstance: id={}, poll={}, timeout={} ...", instId, m_pollingInterval, toStr);
		}
		
		StartableExecution<Void> exec = Executions.toExecution(() -> startInstanceInOwnThread(instance), m_executor);
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
						s_logger.debug("Ignore already running instance: {}, elapsed={}s",
										instance.getId(), watch.stopAndGetElpasedTimeString());
					}
				}
				else {
					throw e;
				}
			}

			// 시작된 인스턴스의 종속 인스턴스들을 시작한다.
			if ( m_recursive ) {
				List<MDTInstance> dependents = instance.getComponentInstanceAll();
				if ( s_logger.isInfoEnabled() ) {
					List<String> depInstIdList = Funcs.map(dependents, MDTInstance::getId);
					s_logger.info("On finished: {} -> Starting dependent instances: {}", instance.getId(), depInstIdList);
				}
				for ( MDTInstance dependent: dependents ) {
					if ( m_triedInstances.add(dependent.getId()) ) {
						startInstanceAsync(dependent);
					}
				}
			}
		}
		catch ( TimeoutException | InterruptedException e ) {
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
		
		/**
		 * MDT 인스턴스 구동에 사용할 MDTInstanceManager를 설정한다.
		 *
		 * @param manager	MDTInstanceManager
		 * @return		빌더 자신
		 */
		public Builder mdtInstanceManager(MDTInstanceManager manager) {
			Preconditions.checkArgument(manager != null, "MDTInstanceManager is null");
			
            m_manager = manager;
            return this;
		}
	
		/**
		 * 구동시킬 MDTInstance의 ID 목록을 설정한다.
		 *
		 * @param idList	MDTInstance ID 목록
		 * @return		빌더 자신
		 */
		public Builder instanceIdList(List<String> idList) {
			Preconditions.checkArgument(idList != null, "MDTInstance id list is null or empty");
			
			m_instanceIdList = idList;
			return this;
		}
		/**
		 * 구동시킬 MDTInstance의 ID 목록을 설정한다.
		 *
		 * @param ids	MDTInstance ID 목록
		 * @return		빌더 자신
		 */
		public Builder instanceIds(String... ids) {
			Preconditions.checkArgument(ids != null, "MDTInstance id list is null or empty");
			
			m_instanceIdList = Lists.newArrayList(ids);
			return this;
		}
		
		/**
		 * 각 MDTInstance 시작 요청 후, 해당 인스턴스의 상태를 주기적으로 확인하는 간격을 설정한다.
		 * <p>
		 * 별도로 지정하지 않는 경우에는 {@link #DEFAULT_POLLING_INTERVAL}이 사용된다.
		 *
		 * @param interval	상태 확인 간격
		 * @return		빌더 자신
		 */
		public Builder pollingInterval(Duration interval) {
			m_pollingInterval = interval;
			return this;
		}
		
		/**
		 * 각 MDTInstance 시작 요청 후, 해당 인스턴스의 실행 결과가 판정될 때까지 대기하는 최대 시간을 설정한다.
		 * <p>
		 * 별도로 지정하지 않는 경우에는 제한 시간이 없다.
		 *
		 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무제한을 의미함.
		 * @return	빌더 자신
		 */
		public Builder timeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		/**
		 * 모든 MDTInstance 시작 요청 후, 해당 인스턴스의 실행 결과가 판정될 때까지 대기하지 않도록 설정한다.
		 *
		 * @param flag	{@code true}이면 대기하지 않음.
		 * @return	빌더 자신
		 */
		public Builder nowait(boolean flag) {
			m_nowait = flag;
			return this;
		}
		
		/**
		 * MDTInstance 관리자에 등록된 모든 비실행중인 MDTInstance들을 시작시키도록 설정한다.
		 *
		 * @param flag	{@code true}이면 모든 비실행중인 MDTInstance들을 시작시킴.
		 * @return	빌더 자신
		 */
		public Builder startAll(boolean flag) {
			m_startAll = flag;
			return this;
		}
		
		/**
		 * MDTInstance 시작 작업을 수행할 쓰레드 풀의 크기를 설정한다.
		 * <p>
		 * 별도로 지정하지 않는 경우에는 1로 설정된다.
		 *
		 * @param nthreads	쓰레드 풀 크기
		 * @return	빌더 자신
		 */
		public Builder nthreads(int nthreads) {
			Preconditions.checkArgument(nthreads > 0, "invalid nthreads: " + nthreads);
            m_executor = Executors.newFixedThreadPool(nthreads);
            return this;
		}
		
		/**
		 * MDTInstance 시작 작업을 수행할 {@link ExecutorService} 객체를 설정한다.
		 * <p>
		 * 별도로 지정하지 않는 경우에는 단일 쓰레드로 구성된 {@link ExecutorService}가 사용된다.
		 * 지정된 ExecutorService를 통해 사용할 수 있는 모든 쓰레드들이 동시에 MDTInstance
		 * 시작 작업을 수행할 수 있다. 이 경우에는 {@link #nthreads(int)}의 설정은 무시된다.
		 *
		 * @param executor	작업을 실행할 {@link ExecutorService} 객체
		 * @return	빌더 자신
		 */
		public Builder executor(ExecutorService executor) {
			m_executor = executor;
			return this;
		}
		
		/**
		 * 종속된 MDTInstance들도 재귀적으로 시작시키도록 설정한다.
		 *
		 * @param flag	{@code true}이면 종속된 MDTInstance들도 재귀적으로 시작시킴.
		 * @return	빌더 자신
		 */
		public Builder recursive(boolean flag) {
			m_recursive = flag;
			return this;
		}
	}
}
