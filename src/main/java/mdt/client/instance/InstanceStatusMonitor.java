package mdt.client.instance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import javax.annotation.concurrent.GuardedBy;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

import utils.Preconditions;
import utils.Tuple;
import utils.func.Unchecked;
import utils.thread.Guard;

import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;

/**
 * 등록된 MDTInstance들의 상태 변경을 주기적으로 감시하는 컴포넌트.
 * <p>
 * Guava의 {@link AbstractScheduledService}를 기반으로 일정 주기마다 등록된 인스턴스들의
 * {@link InstanceDescriptor}를 조회하여 직전 관찰값({@code lastStatus})과 비교한다. 상태가
 * 변경된 인스턴스는 감시 대상에서 제거된 후 등록된 listener에 비동기로 통지된다. 즉, 한
 * 인스턴스에 대해 상태 변경 콜백은 최대 한 번 호출되며, 변경 후에는 자동으로 unwatch된다.
 * <p>
 * 모든 등록/해제/조회 연산은 {@link Guard}로 직렬화된다. listener 호출은
 * 별도의 {@link Executor}를 통해 비동기로 수행되므로, 콜백 내부 작업이 길더라도 다음 polling을
 * 지연시키지 않는다. 기본 executor는 {@link ForkJoinPool#commonPool()}이며, 호출 순서나 격리가
 * 필요하면 생성자에 별도 executor를 주입할 수 있다.
 * <p>
 * 동시성·라이프사이클 관련 주의 사항:
 * <ul>
 *   <li>연속된 두 주기 모두에서 변경이 감지되면 두 비동기 통지 작업이 executor에서 병행 실행될 수
 *       있다. 따라서 listener는 반드시 thread-safe해야 하며 호출 순서에 의존해서는 안 된다.
 *       (단일 쓰레드 executor를 주입하면 순차 실행을 보장할 수 있다.)</li>
 *   <li>{@link #stopAsync()}로 서비스가 중단되더라도 이미 executor에 제출된 listener 호출은
 *       그대로 진행된다. 즉, 서비스 중단 후에도 listener가 호출될 수 있다.</li>
 *   <li>listener 내부에서 발생한 예외는 로깅되지 않고 무시된다.</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InstanceStatusMonitor extends AbstractScheduledService {
	private static final Logger s_logger = LoggerFactory.getLogger(InstanceStatusMonitor.class);

	private final Duration m_interval;

	/** 상태 변경 통지를 받을 listener. */
	@Nullable private final BiConsumer<MDTInstance, MDTInstanceStatus> m_listener;
	/** listener 호출을 비동기로 수행할 executor. */
	private final Executor m_notificationExecutor;

	private final Guard m_guard = Guard.create();
	/** 감시 대상 인스턴스를 ID로 매핑한 등록부. */
	@GuardedBy("m_guard") private final Map<String,Waiter> m_waiters = new HashMap<>();

	/**
	 * 감시 중인 인스턴스 한 건의 스냅샷.
	 *
	 * @param instance   인스턴스 객체. 콜백 호출 시 그대로 전달된다.
	 * @param lastStatus 최근 polling 시점에 관찰된 상태. 다음 주기에서 비교 기준으로 사용된다.
	 */
	private record Waiter(MDTInstance instance, MDTInstanceStatus lastStatus) { }

	/**
	 * 새 상태 감시기를 생성한다.
	 *
	 * @param manager           인스턴스 디스크립터 조회에 사용할 매니저.
	 * @param interval          polling 주기.
	 * @param statusListener	상태 변경 감지 시 호출될 콜백. 변경된 인스턴스와 새 상태를
	 * 							인자로 받는다. {@code null}이면 통지가 비활성화되며 변경된
	 *                          인스턴스는 단순히 감시 대상에서 제거되기만 한다.
	 */
	public InstanceStatusMonitor(Duration interval,
								@Nullable BiConsumer<MDTInstance, MDTInstanceStatus> statusListener) {
		Preconditions.checkNotNullArgument(interval, "polling interval is null");

		m_interval = interval;
		m_listener = statusListener;
		m_notificationExecutor = ForkJoinPool.commonPool();
	}

	/**
	 * 주어진 인스턴스를 감시 대상에 추가한다.
	 * <p>
	 * 현재 상태를 조회하여 {@code lastStatus} 기준값으로 저장한다. 같은 ID로 다시 호출되면
	 * 기존 등록을 덮어쓴다.
	 * <p>
	 * 주기적 점검({@link #runOneIteration()})과 달리 이 메서드에서 발생한 디스크립터 조회 오류는
	 * 흡수되지 않고 호출자에게 그대로 전파된다. 호출 시점에 매니저와의 통신이 가능한 상태여야 한다.
	 *
	 * @param instance 감시할 인스턴스.
	 * @throws RuntimeException 디스크립터 조회 중 통신 오류 등이 발생한 경우.
	 */
	public void watchInstance(MDTInstance instance, MDTInstanceStatus status) {
		Preconditions.checkNotNullArgument(instance, "instance is null");
		Preconditions.checkNotNullArgument(status, "instance status is null");

		String instId = instance.getId();
		Waiter waiter = new Waiter(instance, status);
		m_guard.run(() -> {
			m_waiters.put(instId, waiter);
		});
	}

	/**
	 * 주어진 인스턴스 ID를 감시 대상에서 제거한다.
	 * <p>
	 * 등록되지 않은 ID에 대한 호출은 무시된다.
	 *
	 * @param instId 감시 해제할 인스턴스 ID.
	 */
	public void unwatchInstance(String instId) {
		Preconditions.checkNotNullArgument(instId, "instance ID is null");

		m_guard.run(() -> m_waiters.remove(instId));
	}

	/**
	 * 한 주기 분량의 상태 점검을 수행한다.
	 * <p>
	 * 동작 절차:
	 * <ol>
	 *   <li>현재 등록된 모든 {@link Waiter}를 스냅샷한다.</li>
	 *   <li>각 인스턴스의 최신 디스크립터를 조회하여 {@code lastStatus}와 비교한다.</li>
	 *   <li>변경된 인스턴스는 등록부에서 제거하고 별도 쓰레드에서 listener에 통지한다.</li>
	 * </ol>
	 * 디스크립터 조회 도중 발생한 예외는 각 인스턴스별로 흡수 및 로깅되며, 다음 주기에서 다시
	 * 시도된다.
	 */
	@Override
	protected void runOneIteration() {
		List<Waiter> waiters = m_guard.get(() -> new ArrayList<>(m_waiters.values()));
		if ( waiters.isEmpty() ) {
			return;
		}
		
		// 상태가 바뀐 waiter들을 수집함.
		List<Tuple<Waiter,MDTInstanceStatus>> updateds = new ArrayList<>();
		for ( Waiter waiter: waiters ) {
			try {
				MDTInstanceStatus newStatus = waiter.instance().getStatus();
				if ( waiter.lastStatus != newStatus ) {
					updateds.add(Tuple.of(waiter, newStatus));
				}
			}
			catch ( Exception e ) {
				// 조회 실패한 인스턴스는 일단 무시한다. 다음 주기에 다시 시도할 것이다.
				s_logger.warn("failed to get descriptor for instance: {}", waiter.instance().getId(), e);
			}
		}

		if ( !updateds.isEmpty() ) {
			// 상태가 바뀐 instance를 waiter 목록에서 삭제시킨다.
			// 수집 후 다른 쓰레드가 같은 ID로 새 Waiter를 등록했을 수 있으므로,
			// 동일한 Waiter 인스턴스인 경우에만 제거한다.
			m_guard.run(() -> {
				for ( var updated: updateds ) {
					MDTInstance inst = updated._1().instance();
					m_waiters.remove(inst.getId(), updated._1());
				}
			});

			if ( m_listener != null ) {
				CompletableFuture.runAsync(() -> {
					for ( var tup: updateds ) {
						Unchecked.runOrIgnore(() -> m_listener.accept(tup._1().instance(), tup._2()));
					}
				}, m_notificationExecutor);
			}
		}
	}

	/**
	 * {@link AbstractScheduledService}의 스케줄링 정책을 반환한다.
	 * <p>
	 * 시작 즉시 첫 점검을 수행하고, 이후 매 {@code m_interval}마다 {@link #runOneIteration()}을
	 * 호출한다 (이전 점검 종료 시점 기준).
	 *
	 * @return 고정 지연 스케줄러.
	 */
	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedDelaySchedule(0, m_interval.toMillis(), TimeUnit.MILLISECONDS);
	}
}
