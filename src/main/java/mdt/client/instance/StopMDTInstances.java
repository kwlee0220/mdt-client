package mdt.client.instance;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import utils.InternalException;
import utils.Preconditions;
import utils.UnitUtils;
import utils.async.Executions;
import utils.async.StartableExecution;
import utils.func.Nullables;
import utils.func.CheckedRunnableX;
import utils.func.Try;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;
import utils.thread.Guard;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;

/**
 * 다수의 MDT 인스턴스를 비동기적으로 종료시키는 실행기.
 * <p>
 * {@link Builder}를 통해 종료 대상 인스턴스 목록과 동작 옵션(polling interval, timeout, 대기 여부,
 * 재귀 종료 여부 등)을 설정한 뒤 {@link #run()}을 호출하면 지정된 {@link ExecutorService}를 통해
 * 인스턴스들을 종료시킨다.
 * <ul>
 *   <li>재귀 모드가 아닌 경우: 대상 인스턴스들 중 RUNNING 상태인 것들에 대해 종료 작업을 모두 제출하고,
 *       모든 작업이 끝날 때까지 대기한다.</li>
 *   <li>재귀 모드인 경우: 대상 인스턴스와 그 종속(component) 인스턴스들을 재귀적으로 수집한 뒤,
 *       종속 인스턴스가 모두 종료된 인스턴스부터 차례로 종료시킨다. 즉, 종속 인스턴스가 먼저 종료된다.</li>
 * </ul>
 * {@link Builder#stopAll(boolean) stopAll}이 설정된 경우에는 RUNNING 상태인 모든 인스턴스가 대상이 되며,
 * 이때는 재귀 모드 설정이 무시된다.
 * <p>
 * 실행기는 {@link Builder#executor(ExecutorService)}로 주어진 것을 쓰고, 주어지지 않으면 {@link #run()}마다
 * {@link Builder#nthreads(int)} 크기(기본 1)의 쓰레드 풀을 새로 만들어 쓰고 종료 시 {@code shutdown()} 한다.
 * 주어진 실행기는 종료시키지 않으므로 호출자가 관리한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class StopMDTInstances implements CheckedRunnableX<InterruptedException> {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstances.class);
	private static final Duration DEFAULT_POLLING_INTERVAL = UnitUtils.parseDuration("1s");

	private final MDTInstanceManager m_manager;
	private final List<String> m_instanceIdList;
	private final Duration m_pollingInterval;
	private final Duration m_timeout;
	private final boolean m_nowait;
	private final boolean m_stopAll;
	@Nullable private final ExecutorService m_givenExecutor;	// 호출자가 준 실행기. 종료시키지 않는다.
	private final int m_nthreads;								// 실행기를 직접 만들 때의 쓰레드 수
	private final boolean m_recursive;
	private volatile ExecutorService m_executor;				// 현재 run()이 사용하는 실행기
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private int m_nfinished = 0;
	// 종료 대상 instance에 해당하는 dependent instance들의 집합을 관리
	@GuardedBy("m_guard") private final Map<String,Set<String>> m_dependencyMap = Maps.newConcurrentMap();
	@GuardedBy("m_guard") private final Map<String,MDTInstance> m_instanceMap = Maps.newHashMap();
	// 종료 작업을 제출했으나 아직 끝나지 않은 instance 식별자들. 다시 제출하지 않기 위해 관리한다.
	@GuardedBy("m_guard") private final Set<String> m_inFlight = Sets.newHashSet();
	
	private StopMDTInstances(@Nonnull Builder builder) {
		Preconditions.checkNotNullArgument(builder, "Builder is not provided");
		Preconditions.checkNotNullArgument(builder.m_manager, "MDTInstanceManager is not set");
		
		m_manager = builder.m_manager;
        m_instanceIdList = builder.m_instanceIdList;
        m_pollingInterval = Nullables.orElse(builder.m_pollingInterval, DEFAULT_POLLING_INTERVAL);
        m_timeout = builder.m_timeout;
        m_nowait = builder.m_nowait;
        m_stopAll = builder.m_stopAll;
        m_givenExecutor = builder.m_executor;
        m_nthreads = builder.m_nthreads;
        m_recursive = builder.m_recursive;
	}
	
	/**
	 * 설정된 인스턴스들을 종료시킨다.
	 * <p>
	 * {@code instanceIdList}로 지정된 인스턴스 중 조회에 실패한 것은 표준 출력에 원인을 출력하고 건너뛴다.
	 * 종료 요청 시점에 RUNNING 상태가 아닌 인스턴스에 대해서는 아무 작업도 하지 않는다.
	 * 모든 대상 인스턴스가 처리될 때까지 대기한다. 직접 만든 실행기는 마지막에 {@code shutdown()} 한다.
	 *
	 * @throws InterruptedException	대기 도중 인터럽트된 경우.
	 * @throws InternalException	재귀 모드에서 종속 관계에 순환이 있어 종료할 인스턴스를 고를 수 없는 경우.
	 */
	@Override
	public void run() throws InterruptedException {
		boolean ownsExecutor = (m_givenExecutor == null);
		m_executor = ownsExecutor ? Executors.newFixedThreadPool(m_nthreads) : m_givenExecutor;
		try {
			runWith();
		}
		finally {
			if ( ownsExecutor ) {
				m_executor.shutdown();
			}
		}
	}
	
	private void runWith() throws InterruptedException {
		boolean recursive = m_recursive;
		List<? extends MDTInstance> targetInstList;
		if ( m_stopAll ) {
			targetInstList = FStream.from(m_manager.getInstanceAll())
									.filter(inst -> inst.getStatus() == MDTInstanceStatus.RUNNING)
									.toList();
			
			// 전체를 stop 시키는 경우는 굳이 recursive하게 instance들을 stop 시킬 필요가 없다.
			recursive = false;
		}
		else {
			targetInstList = FStream.from(m_instanceIdList)
									// 식별자에 해당하는 인스턴스를 접근하지 못할 수도 있음.
									.flatMapTry(this::tryGetInstance)
									.toList();
		}

		if ( !recursive ) {
			List<StartableExecution<Void>> execList
				= FStream.from(targetInstList)
						.map(inst -> {
							StartableExecution<Void> exec = Executions.toExecution(() -> stopInstance(inst),
																					m_executor);
							exec.start();
							return exec;
						})
						.toList();
			FStream.from(execList).forEachOrThrow(StartableExecution::waitForFinished);
			return;
		} 
		
		m_guard.run(() -> {
			m_instanceMap.clear();
			m_dependencyMap.clear();
			m_inFlight.clear();
			m_nfinished = 0;
			for ( MDTInstance instance: targetInstList ) {
				m_instanceMap.putIfAbsent(instance.getId(), instance);
				buildDependencies(instance);
			}
		});
		
		int remains = m_guard.get(() -> m_instanceMap.size());
		while ( remains > 0 ) {
			// Dependency가 없고 종료 작업이 진행 중이지 않은 instance를 찾는다.
			List<MDTInstance> freeAgents = m_guard.get(this::collectFreeAgentInGuard);
			if ( freeAgents.isEmpty() && m_guard.get(m_inFlight::isEmpty) ) {
				// 진행 중인 작업도 없는데 고를 instance가 없으면 종속 관계에 순환이 있는 것이다.
				throw new InternalException("failed to find free MDTInstance for stopping");
			}
			
			for ( MDTInstance freeAgent: freeAgents ) {
				if ( freeAgent.getStatus() == MDTInstanceStatus.RUNNING ) {
					StartableExecution<Void> exec = Executions.toExecution(() -> stopInstance(freeAgent),
																			m_executor);
					exec.whenFinished(result -> onInstanceFinished(freeAgent));
					exec.start();
				}
				else {
					onInstanceFinished(freeAgent);
				}
			}
			
			remains = m_guard.awaitCondition(() -> wakeUpCondition())
							.andGet(() -> m_instanceMap.size());
		}
	}
	
	// 고른 instance들은 진행 중(m_inFlight)으로 표시되어 다음 호출에서 다시 골라지지 않는다.
	private List<MDTInstance> collectFreeAgentInGuard() {
		m_nfinished = 0;
		List<MDTInstance> free = KeyValueFStream.from(m_dependencyMap)
												.filter(kv -> kv.value().isEmpty() && !m_inFlight.contains(kv.key()))
												.map(kv -> m_instanceMap.get(kv.key()))
												.toList();
		free.forEach(inst -> m_inFlight.add(inst.getId()));
		return free;
	}
	private void onInstanceFinished(MDTInstance instance) {
		String finishedId = instance.getId();
		m_guard.run(() -> {
			// 종료된 instance에 dependent한 instance들의 dependencyMap에서 제거한다.
			KeyValueFStream.from(m_dependencyMap)
							.forEach(kv -> kv.value().remove(finishedId));
			m_instanceMap.remove(finishedId);
			m_dependencyMap.remove(finishedId);
			m_inFlight.remove(finishedId);
			++m_nfinished;
		});
	}
	private boolean wakeUpCondition() {
		return m_instanceMap.isEmpty() || (m_nfinished > 0 && existsFreeAgentsInGuard());
	}
	private boolean existsFreeAgentsInGuard() {
		return KeyValueFStream.from(m_dependencyMap)
								.exists(kv -> kv.value().isEmpty() && !m_inFlight.contains(kv.key()));
	}
	
	private Try<MDTInstance> tryGetInstance(String id) {
		return Try.of(() -> m_manager.getInstance(id))
					.ifFailed(error -> {
						System.out.printf("Cannot get MDTInstance: id=%s, cause=%s%n", id, error);
					});
	}
	
	private void buildDependencies(MDTInstance start) {
		Set<String> dependents = m_dependencyMap.computeIfAbsent(start.getId(), k -> Sets.newHashSet());
		
		List<MDTInstance> components = start.getComponentInstanceAll();
		for ( MDTInstance comp: components ) {
			m_instanceMap.putIfAbsent(comp.getId(), comp);
			dependents.add(comp.getId());
			
			buildDependencies(comp);
		}
	}
	
	private void stopInstance(MDTInstance instance) throws TimeoutException, InterruptedException,
																ExecutionException {
		if ( instance.getStatus() == MDTInstanceStatus.RUNNING ) {
			if ( s_logger.isDebugEnabled() ) {
				s_logger.debug("Stopping MDTInstance[{}]", instance.getId());
			}
			
			if ( m_nowait ) {
				instance.stop(null, null);
			}
			else {
				instance.stop(m_pollingInterval, m_timeout);
			}
			if ( s_logger.isInfoEnabled() ) {
				s_logger.info("Stopped MDTInstance[{}]", instance.getId());
			}
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
	 * {@link StopMDTInstances} 빌더.
	 * <p>
	 * {@link #mdtInstanceManager(MDTInstanceManager)}는 반드시 설정되어야 한다.
	 */
	public static class Builder {
		private MDTInstanceManager m_manager;
		private List<String> m_instanceIdList = null;
		private boolean m_nowait = false;
		private boolean m_stopAll = false;
		private Duration m_pollingInterval = null;
		private Duration m_timeout = null;
		private ExecutorService m_executor = null;
		private int m_nthreads = 1;
		private boolean m_recursive = false;
		
		/**
		 * 현재까지 설정된 값으로 {@link StopMDTInstances}를 생성한다.
		 *
		 * @return	새 {@code StopMDTInstances} 인스턴스.
		 * @throws IllegalArgumentException	{@link MDTInstanceManager}가 설정되지 않은 경우.
		 */
		public StopMDTInstances build() {
			return new StopMDTInstances(this);
		}
		
		/**
		 * MDT 인스턴스 종료에 사용할 {@link MDTInstanceManager}를 설정한다.
		 *
		 * @param manager	MDTInstanceManager.
		 * @return	빌더 자신.
		 */
		public Builder mdtInstanceManager(MDTInstanceManager manager) {
            m_manager = manager;
            return this;
		}
	
		/**
		 * 종료시킬 MDTInstance의 ID 목록을 설정한다.
		 *
		 * @param idList	MDTInstance ID 목록.
		 * @return	빌더 자신.
		 */
		public Builder instanceIdList(List<String> idList) {
			m_instanceIdList = idList;
			return this;
		}
		/**
		 * 종료시킬 MDTInstance의 ID 목록을 설정한다.
		 *
		 * @param ids	MDTInstance ID 목록.
		 * @return	빌더 자신.
		 */
		public Builder instanceIds(String... ids) {
			m_instanceIdList = Lists.newArrayList(ids);
			return this;
		}
		
		/**
		 * 각 MDTInstance에 종료 요청만 보내고 종료 완료까지 기다리지 않도록 설정한다.
		 * <p>
		 * 이 경우 {@link #pollingInterval(Duration)}와 {@link #timeout(Duration)} 설정은 사용되지 않는다.
		 *
		 * @param flag	{@code true}이면 종료 완료를 기다리지 않음.
		 * @return	빌더 자신.
		 */
		public Builder nowait(boolean flag) {
			m_nowait = flag;
			return this;
		}
		
		/**
		 * 각 MDTInstance 종료 요청 후, 종료 완료 여부를 확인하는 간격을 설정한다.
		 * <p>
		 * 별도로 지정하지 않거나 {@code null}인 경우에는 1초가 사용된다.
		 *
		 * @param interval	상태 확인 간격.
		 * @return	빌더 자신.
		 */
		public Builder pollingInterval(Duration interval) {
			m_pollingInterval = interval;
			return this;
		}
		
		/**
		 * 각 MDTInstance의 종료 완료를 기다리는 최대 시간을 설정한다.
		 * <p>
		 * 별도로 지정하지 않는 경우에는 제한 시간이 없다.
		 *
		 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무제한을 의미함.
		 * @return	빌더 자신.
		 */
		public Builder timeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		/**
		 * MDTInstance 관리자에 등록된 모든 RUNNING 상태의 MDTInstance들을 종료시키도록 설정한다.
		 * <p>
		 * 설정된 경우에는 ID 목록과 재귀 모드 설정이 무시된다.
		 *
		 * @param flag	{@code true}이면 RUNNING 상태인 모든 MDTInstance들을 종료시킴.
		 * @return	빌더 자신.
		 */
		public Builder stopAll(boolean flag) {
			m_stopAll = flag;
			return this;
		}
		
		/**
		 * MDTInstance 종료 작업을 수행할 {@link ExecutorService} 객체를 설정한다.
		 * <p>
		 * {@link #nthreads(int)} 설정을 대체하므로 나중에 호출된 것이 적용된다. 주어진 실행기는
		 * {@link StopMDTInstances#run()}이 종료시키지 않으므로 호출자가 관리한다.
		 *
		 * @param executor	작업을 실행할 {@link ExecutorService} 객체.
		 * @return	빌더 자신.
		 */
		public Builder executor(ExecutorService executor) {
			m_executor = executor;
			m_nthreads = 1;
			return this;
		}
		
		/**
		 * 종료 작업 실행기의 쓰레드 수를 설정한다.
		 * <p>
		 * {@link StopMDTInstances#run()}마다 주어진 크기의 고정 쓰레드 풀을 만들어 쓰고 종료 시 {@code shutdown()}
		 * 한다. {@link #executor(ExecutorService)} 설정을 대체하므로 나중에 호출된 것이 적용된다. 기본값은 1이다.
		 *
		 * @param nthreads	쓰레드 풀 크기.
		 * @return	빌더 자신.
		 * @throws IllegalArgumentException	{@code nthreads}가 1 미만인 경우.
		 */
		public Builder nthreads(int nthreads) {
			Preconditions.checkArgument(nthreads > 0, "invalid nthreads: %s", nthreads);
			m_nthreads = nthreads;
			m_executor = null;
            return this;
		}
		
		/**
		 * 대상 MDTInstance들의 종속(component) MDTInstance들도 재귀적으로 종료시키도록 설정한다.
		 * <p>
		 * 종속 인스턴스가 모두 종료된 뒤에 해당 인스턴스가 종료된다.
		 *
		 * @param flag	{@code true}이면 종속된 MDTInstance들도 재귀적으로 종료시킴.
		 * @return	빌더 자신.
		 */
		public Builder recursive(boolean flag) {
			m_recursive = flag;
			return this;
		}
	}
}
