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
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import utils.InternalException;
import utils.UnitUtils;
import utils.async.Executions;
import utils.async.Guard;
import utils.async.StartableExecution;
import utils.func.CheckedRunnableX;
import utils.func.FOption;
import utils.func.Try;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class StopMDTInstances implements CheckedRunnableX<InterruptedException> {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstances.class);
	private static final Duration DEFAULT_POLLING_INTERVAL = UnitUtils.parseDuration("1s");
	private static final ExecutorService DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();

	private final MDTInstanceManager m_manager;
	private final List<String> m_instanceIdList;
	private final Duration m_pollingInterval;
	private final Duration m_timeout;
	private final boolean m_nowait;
	private final boolean m_stopAll;
	private final ExecutorService m_executor;
	private boolean m_recursive;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private int m_nfinished = 0;
	// 종료 대상 instance에 해당하는 dependent instance들의 집합을 관리
	@GuardedBy("m_guard") private final Map<String,Set<String>> m_dependencyMap = Maps.newConcurrentMap();
	@GuardedBy("m_guard") private final Map<String,MDTInstance> m_instanceMap = Maps.newHashMap();
	
	private StopMDTInstances(@Nonnull Builder builder) {
		Preconditions.checkNotNull(builder != null, "Builder is not provided");
		Preconditions.checkNotNull(builder.m_manager, "MDTInstanceManager is not set");
		
		m_manager = builder.m_manager;
        m_instanceIdList = builder.m_instanceIdList;
        m_pollingInterval = FOption.getOrElse(builder.m_pollingInterval, DEFAULT_POLLING_INTERVAL);
        m_timeout = builder.m_timeout;
        m_nowait = builder.m_nowait;
        m_stopAll = builder.m_stopAll;
        m_executor = FOption.getOrElse(builder.m_executor, DEFAULT_EXECUTOR);
        m_recursive = builder.m_recursive;
	}
	
	@Override
	public void run() throws InterruptedException {
		List<? extends MDTInstance> targetInstList;
		if ( m_stopAll ) {
			targetInstList = FStream.from(m_manager.getInstanceAll())
									.filter(inst -> inst.getStatus() == MDTInstanceStatus.RUNNING)
									.toList();
			
			// 전체를 stop 시키는 경우는 굳이 recursive하게 instance들을 stop 시킬 필요가 없다.
			m_recursive = false;
		}
		else {
			targetInstList = FStream.from(m_instanceIdList)
									// 식별자에 해당하는 인스턴스를 접근하지 못할 수도 있음.
									.flatMapTry(this::tryGetInstance)
									.toList();
		}

		if ( !m_recursive ) {
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
		
		for ( MDTInstance instance: targetInstList ) {
			m_instanceMap.putIfAbsent(instance.getId(), instance);
			buildDependencies(instance);
		}
		
		try {
			int remains = m_guard.get(() -> m_instanceMap.size());
			while ( remains > 0 ) {
				// Dependency가 없는 instance를 찾는다.
				List<MDTInstance> freeAgents = m_guard.get(this::collectFreeAgentInGuard);
				if ( freeAgents.isEmpty() ) {
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
		finally {
			m_executor.shutdown();
		}
	}
	
	private List<MDTInstance> collectFreeAgentInGuard() {
		m_nfinished = 0;
		return KeyValueFStream.from(m_dependencyMap)
								.filterValue(deps -> deps.isEmpty())
								.map(kv -> m_instanceMap.get(kv.key()))
								.toList();
	}
	private void onInstanceFinished(MDTInstance instance) {
		String finishedId = instance.getId();
		m_guard.run(() -> {
			// 종료된 instance에 dependent한 instance들의 dependencyMap에서 제거한다.
			KeyValueFStream.from(m_dependencyMap)
							.forEach(kv -> kv.value().remove(finishedId));
			m_instanceMap.remove(finishedId);
			m_dependencyMap.remove(finishedId);
			++m_nfinished;
		});
	}
	private boolean wakeUpCondition() {
		return m_instanceMap.isEmpty() || (m_nfinished > 0 && existsFreeAgentsInGuard());
	}
	private boolean existsFreeAgentsInGuard() {
		return KeyValueFStream.from(m_dependencyMap).exists(kv -> kv.value().isEmpty());
	}
	
	private Try<MDTInstance> tryGetInstance(String id) {
		return Try.get(() -> m_manager.getInstance(id))
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
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private MDTInstanceManager m_manager;
		private List<String> m_instanceIdList = null;
		private boolean m_nowait = false;
		private boolean m_stopAll = false;
		private Duration m_pollingInterval = null;
		private Duration m_timeout = null;
		private ExecutorService m_executor = null;
		private boolean m_recursive = false;
		
		public StopMDTInstances build() {
			return new StopMDTInstances(this);
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
		
		public Builder nowait(boolean flag) {
			m_nowait = flag;
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
		
		public Builder stopAll(boolean flag) {
			m_stopAll = flag;
			return this;
		}
		
		public Builder executor(ExecutorService executor) {
			m_executor = executor;
			return this;
		}
		
		public Builder nthreads(int nthreads) {
            m_executor = Executors.newFixedThreadPool(nthreads);
            return this;
		}
		
		public Builder recursive(boolean flag) {
			m_recursive = flag;
			return this;
		}
	}
}
