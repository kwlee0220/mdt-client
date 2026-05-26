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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.Preconditions;
import utils.StopWatch;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.Executions;
import utils.async.StartableExecution;
import utils.func.CheckedRunnableX;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Optionals;
import utils.stream.FStream;
import utils.thread.Guard;

import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;

/**
 * 다수의 MDT 인스턴스를 비동기적으로 시작시키는 실행기.
 * <p>
 * {@link Builder}를 통해 시작 대상 인스턴스 목록과 동작 옵션(polling interval, timeout, 동시성,
 * 재귀 시작 여부 등)을 설정한 뒤 {@link #run()}을 호출하면 지정된 {@link ExecutorService}를 통해
 * 인스턴스들을 병렬로 시작시킨다.
 * <p>
 * 주요 동작 옵션:
 * <ul>
 *   <li>{@link Builder#startAll(boolean) startAll}: 매니저에 등록된 비실행 인스턴스 전부를 시작.</li>
 *   <li>{@link Builder#nowait(boolean) nowait}: 시작 요청만 보내고 완료까지 기다리지 않음.</li>
 *   <li>{@link Builder#recursive(boolean) recursive}: 시작된 인스턴스의 종속 인스턴스도 재귀적으로 시작.</li>
 * </ul>
 * <p>
 * 진행 상황은 내부 카운터({@code m_pendingCount}, {@code m_startingCount})와 시도 이력
 * ({@code m_triedInstances})으로 추적되며, 모든 상태 접근은 {@link Guard}로 직렬화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class StartMDTInstances implements CheckedRunnableX<InterruptedException> {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstances.class);
	/** 기본 상태 확인 주기 (1초). */
	private static final Duration DEFAULT_POLLING_INTERVAL = UnitUtils.parseDuration("1s");
	/** 기본 실행기 (단일 쓰레드). */
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
	/** 시작 요청은 들어왔으나 아직 쓰레드를 할당받지 못해 대기 중인 인스턴스 수. */
	@GuardedBy("m_guard") private int m_pendingCount = 0;
	/** 쓰레드를 할당받아 실제 시작 작업이 진행 중인 인스턴스 수. */
	@GuardedBy("m_guard") private int m_startingCount = 0;
	/** 시작을 한 번이라도 시도한 인스턴스 ID 집합. 재귀 시작 시 중복 시도를 방지하는 용도. */
	@GuardedBy("m_guard") private Set<String> m_triedInstances = Sets.newHashSet();

	/**
	 * 빌더에서 설정된 값으로 {@code StartMDTInstances}를 생성한다.
	 *
	 * @param builder 설정값을 담은 빌더.
	 */
	private StartMDTInstances(@Nonnull Builder builder) {
		Preconditions.checkNotNullArgument(builder, "Builder is not provided");
		Preconditions.checkNotNullArgument(builder.m_manager, "MDTInstanceManager is not set");
		
		m_manager = builder.m_manager;
        m_instanceIdList = builder.m_instanceIdList;
        m_pollingInterval = Optionals.getOrElse(builder.m_pollingInterval, DEFAULT_POLLING_INTERVAL);
        m_timeout = builder.m_timeout;
        m_nowait = builder.m_nowait;
        m_startAll = builder.m_startAll;
        m_executor = Optionals.getOrElse(builder.m_executor, DEFAULT_EXECUTOR);
        m_recursive = builder.m_recursive;
	}
	
	/**
	 * 설정된 인스턴스들을 비동기적으로 시작시킨다.
	 * <p>
	 * 동작 절차:
	 * <ol>
	 *   <li>{@code startAll}이 켜져 있으면 매니저로부터 비실행 인스턴스 전부를, 그렇지 않으면
	 *       {@code instanceIdList}에 지정된 인스턴스만 조회한다. 개별 조회 실패는 표준 출력에
	 *       경고를 남기고 다음 인스턴스로 넘어간다.</li>
	 *   <li>각 대상 인스턴스에 대해 {@link #startInstanceAsync(MDTInstance)}를 호출하여
	 *       {@link ExecutorService}에 시작 작업을 제출한다.</li>
	 *   <li>{@code nowait}와 {@code recursive} 설정에 따라 대기 조건이 달라진다.
	 *       대기하지 않더라도 마지막 요청들이 실제로 시작될 시간을 주기 위해 1초간 대기한다.</li>
	 * </ol>
	 * 마지막에는 {@link ExecutorService#shutdown()}을 호출하여 실행기를 정리한다.
	 *
	 * @throws InterruptedException 대기 도중 인터럽트된 경우.
	 */
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
	
	/**
	 * 단일 MDTInstance에 대해 시작 작업을 {@link ExecutorService}에 제출한다.
	 * <p>
	 * 작업 제출 시 {@code m_pendingCount}를 증가시키며, 쓰레드 풀이 가득 찬 경우에는
	 * 큐에서 대기하다가 가용 쓰레드가 생기면 {@link #startInstanceInOwnThread(MDTInstance)}가
	 * 실행된다. 시작 작업이 실패하면 표준 출력에 원인을 출력한다.
	 *
	 * @param instance 시작할 MDTInstance.
	 * @return 제출된 비동기 실행 객체.
	 */
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
	
	/**
	 * 작업 쓰레드가 할당된 후 실제 인스턴스 시작 절차를 수행한다.
	 * <p>
	 * 카운터를 업데이트한 뒤 {@link MDTInstance#start(Duration, Duration)}을 호출하여 인스턴스의
	 * 상태가 RUNNING이 될 때까지 또는 timeout이 만료될 때까지 대기한다. 이미 RUNNING 상태인
	 * 인스턴스에 대한 {@link InvalidResourceStatusException}은 정상 흐름으로 간주하여 무시한다.
	 * <p>
	 * {@code recursive}가 켜져 있으면 시작에 성공한 인스턴스의 종속 컴포넌트 인스턴스 목록을
	 * 조회하여 아직 시도되지 않은 인스턴스들을 추가로 비동기 시작시킨다.
	 *
	 * @param instance 시작할 MDTInstance.
	 * @throws InvalidResourceStatusException 시작 대상 인스턴스가 시작 불가능한 상태이고
	 *                                        RUNNING도 아닌 경우.
	 * @throws TimeoutException               설정된 timeout 내에 RUNNING 상태에 도달하지 못한 경우.
	 * @throws InterruptedException           대기 도중 인터럽트된 경우.
	 * @throws ExecutionException             하위 비동기 실행이 실패한 경우.
	 */
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
				s_logger.info("Started MDTInstance[{}]", instance.getId());
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
	
	/**
	 * 새 {@link Builder} 인스턴스를 생성한다.
	 *
	 * @return 빈 빌더.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link StartMDTInstances} 빌더.
	 * <p>
	 * 시작 대상, 대기 정책, 동시성, 재귀 시작 여부 등을 단계적으로 설정한 뒤 {@link #build()}로
	 * 완성된 실행기를 얻는다.
	 */
	public static class Builder {
		private MDTInstanceManager m_manager;
		private List<String> m_instanceIdList = null;
		private Duration m_pollingInterval = null;
		private Duration m_timeout = null;
		private boolean m_nowait = false;
		private boolean m_startAll = false;
		private ExecutorService m_executor = null;
		private boolean m_recursive = false;
		
		/**
		 * 현재까지 설정된 값으로 {@link StartMDTInstances}를 생성한다.
		 *
		 * @return 새 {@code StartMDTInstances} 인스턴스.
		 */
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
			Preconditions.checkNotNullArgument(manager, "MDTInstanceManager is null");
			
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
			Preconditions.checkNotNullArgument(idList, "MDTInstance id list is null or empty");
			
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
			Preconditions.checkNotNullArgument(ids, "MDTInstance id list is null or empty");
			
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
