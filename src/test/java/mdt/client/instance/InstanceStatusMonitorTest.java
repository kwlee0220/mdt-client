package mdt.client.instance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;


/**
 * {@link InstanceStatusMonitor}의 핵심 동작 단위 테스트.
 * <p>
 * 전체 비동기 서비스 라이프사이클({@code startAsync()}/{@code stopAsync()})을 돌리는 대신, 같은 패키지에서
 * protected {@code runOneIteration()}을 직접 호출하여 한 주기 분량의 처리만 검증한다. 상태 조회는
 * mock된 {@link MDTInstance#getStatus()}로 시뮬레이션하고, listener 통지는
 * {@link ForkJoinPool#commonPool()}에서 비동기로 실행되므로 검증 시에는 {@link CountDownLatch}로
 * 동기화한다.
 */
public class InstanceStatusMonitorTest {
	private static final Duration INTERVAL = Duration.ofMillis(100);
	private static final long ASYNC_WAIT_MS = 1_000;

	// --- core behaviors ---

	/**
	 * 등록된 waiter가 없으면 {@code runOneIteration()}이 listener를 호출하지 않고 조용히 반환해야 한다.
	 */
	@Test
	public void emptyWaiterListDoesNothing() {
		@SuppressWarnings("unchecked")
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = mock(BiConsumer.class);
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		monitor.runOneIteration();

		verify(listener, never()).accept(any(), any());
	}

	/**
	 * 상태가 변하지 않은 인스턴스에 대해서는 listener가 호출되지 않고, waiter도 등록부에 그대로 남아
	 * 다음 주기에 재조회되어야 한다.
	 */
	@Test
	public void unchangedStatusDoesNotNotify() throws Exception {
		@SuppressWarnings("unchecked")
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = mock(BiConsumer.class);
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.STARTING);
		monitor.watchInstance(inst, MDTInstanceStatus.STARTING);

		monitor.runOneIteration();
		monitor.runOneIteration();

		// 두 주기 모두 상태가 그대로 → listener 호출 없음, 매니저 조회 없음 (기본 mocking),
		// instance.getStatus()는 각 주기마다 호출.
		verify(listener, never()).accept(any(), any());
		verify(inst, times(2)).getStatus();
	}

	/**
	 * 상태가 변경된 인스턴스는 listener에 새 상태로 통지되고, 다음 주기에는 등록부에서 제거되어
	 * 다시 조회되지 않아야 한다.
	 */
	@Test
	public void changedStatusNotifiesAndRemoves() throws Exception {
		CountDownLatch notified = new CountDownLatch(1);
		AtomicReference<MDTInstance> seenInst = new AtomicReference<>();
		AtomicReference<MDTInstanceStatus> seenStatus = new AtomicReference<>();
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = (i, s) -> {
			seenInst.set(i);
			seenStatus.set(s);
			notified.countDown();
		};
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.STARTING);
		monitor.watchInstance(inst, MDTInstanceStatus.STARTING);
		setStatus(inst, MDTInstanceStatus.RUNNING);

		monitor.runOneIteration();
		Assertions.assertTrue(notified.await(ASYNC_WAIT_MS, TimeUnit.MILLISECONDS),
							"listener 비동기 통지 대기 timeout");
		Assertions.assertSame(inst, seenInst.get());
		Assertions.assertEquals(MDTInstanceStatus.RUNNING, seenStatus.get());

		// 두 번째 주기: 이미 unwatch 되었으므로 getStatus 추가 호출 없음.
		monitor.runOneIteration();
		verify(inst, times(1)).getStatus();
	}

	/**
	 * 여러 waiter가 섞여 있을 때, 상태가 바뀐 인스턴스만 통지·제거되고 나머지는 그대로 남아야 한다.
	 */
	@Test
	public void multipleWaitersOnlyChangedOnesProcessed() throws Exception {
		CountDownLatch notified = new CountDownLatch(1);
		AtomicReference<MDTInstance> seenInst = new AtomicReference<>();
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = (i, s) -> {
			seenInst.set(i);
			notified.countDown();
		};
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance changed = mockInstance("changed", MDTInstanceStatus.STARTING);
		MDTInstance same = mockInstance("same", MDTInstanceStatus.RUNNING);
		monitor.watchInstance(changed, MDTInstanceStatus.STARTING);
		monitor.watchInstance(same, MDTInstanceStatus.RUNNING);

		setStatus(changed, MDTInstanceStatus.RUNNING);
		// "same"은 그대로 둠.

		monitor.runOneIteration();
		Assertions.assertTrue(notified.await(ASYNC_WAIT_MS, TimeUnit.MILLISECONDS),
							"listener 비동기 통지 대기 timeout");
		Assertions.assertSame(changed, seenInst.get());

		// "same"은 다음 주기에도 조회되어야 함 (총 2번).
		monitor.runOneIteration();
		verify(same, times(2)).getStatus();
		// "changed"는 첫 주기 후 제거됨 → 총 1번.
		verify(changed, times(1)).getStatus();
	}

	/**
	 * listener가 {@code null}이어도 변경된 인스턴스는 등록부에서 제거되어야 한다.
	 * (통지만 비활성화되고 unwatch 동작은 유지된다.)
	 */
	@Test
	public void nullListenerStillRemovesChangedInstance() {
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, null);

		MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.STARTING);
		monitor.watchInstance(inst, MDTInstanceStatus.STARTING);
		setStatus(inst, MDTInstanceStatus.RUNNING);

		monitor.runOneIteration();
		monitor.runOneIteration();

		// 첫 주기에서 변경 감지 후 제거 → 두 번째 주기에서 getStatus 호출 없음.
		verify(inst, times(1)).getStatus();
	}

	/**
	 * 상태 조회 도중 예외가 발생하면 해당 waiter는 흡수·로깅되고, 같은 주기의 다른 인스턴스 처리는
	 * 계속되어야 한다. 또한 실패한 waiter는 등록부에 남아 다음 주기에서 다시 시도된다.
	 */
	@Test
	public void statusLookupExceptionIsSwallowed() throws Exception {
		CountDownLatch notified = new CountDownLatch(1);
		AtomicReference<MDTInstance> seenInst = new AtomicReference<>();
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = (i, s) -> {
			seenInst.set(i);
			notified.countDown();
		};
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance ok = mockInstance("ok", MDTInstanceStatus.STARTING);
		MDTInstance fail = mockInstance("fail", MDTInstanceStatus.STARTING);
		monitor.watchInstance(ok, MDTInstanceStatus.STARTING);
		monitor.watchInstance(fail, MDTInstanceStatus.STARTING);

		// "ok"는 RUNNING으로 변경 → 통지 대상.
		setStatus(ok, MDTInstanceStatus.RUNNING);
		// "fail"은 getStatus()에서 예외 발생.
		when(fail.getStatus()).thenThrow(new RuntimeException("boom"));

		monitor.runOneIteration();

		// "ok"는 정상 통지.
		Assertions.assertTrue(notified.await(ASYNC_WAIT_MS, TimeUnit.MILLISECONDS),
							"listener 비동기 통지 대기 timeout");
		Assertions.assertSame(ok, seenInst.get());

		// "fail"은 흡수되어 등록부에 남음 → 다음 주기에서 재시도.
		monitor.runOneIteration();
		verify(fail, atLeastOnce()).getStatus();
	}

	/**
	 * {@link InstanceStatusMonitor#unwatchInstance(String)}로 제거한 인스턴스는 다음 주기에서
	 * 조회되지 않아야 한다.
	 */
	@Test
	public void unwatchInstanceRemovesFromRegistry() {
		@SuppressWarnings("unchecked")
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = mock(BiConsumer.class);
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.STARTING);
		monitor.watchInstance(inst, MDTInstanceStatus.STARTING);
		monitor.unwatchInstance("inst-1");

		monitor.runOneIteration();

		verify(inst, never()).getStatus();
		verify(listener, never()).accept(any(), any());
	}

	/**
	 * 같은 ID로 {@link InstanceStatusMonitor#watchInstance(MDTInstance, MDTInstanceStatus)}를
	 * 다시 호출하면 기존 등록을 덮어쓰고 새 {@code lastStatus} 기준값을 사용해야 한다.
	 */
	@Test
	public void watchInstanceOverwritesExistingRegistration() {
		@SuppressWarnings("unchecked")
		BiConsumer<MDTInstance, MDTInstanceStatus> listener = mock(BiConsumer.class);
		InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, listener);

		MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.RUNNING);
		// 1차 등록: 기준 STARTING (실제 상태는 RUNNING으로 mock).
		monitor.watchInstance(inst, MDTInstanceStatus.STARTING);
		// 재등록: 기준을 RUNNING으로 갱신.
		monitor.watchInstance(inst, MDTInstanceStatus.RUNNING);

		monitor.runOneIteration();

		// 기준(RUNNING) == 현재(RUNNING) → 통지 없음.
		verify(listener, never()).accept(any(), any());
	}

	// --- constructor argument validation ---

	@Test
	public void constructorRejectsNullInterval() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new InstanceStatusMonitor(null, (i, s) -> {});
		});
	}

	@Test
	public void watchInstanceRejectsNullInstance() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, null);
			monitor.watchInstance(null, MDTInstanceStatus.STARTING);
		});
	}

	@Test
	public void watchInstanceRejectsNullStatus() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, null);
			MDTInstance inst = mockInstance("inst-1", MDTInstanceStatus.STARTING);
			monitor.watchInstance(inst, null);
		});
	}

	@Test
	public void unwatchInstanceRejectsNullId() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			InstanceStatusMonitor monitor = new InstanceStatusMonitor(INTERVAL, null);
			monitor.unwatchInstance(null);
		});
	}

	// --- helpers ---

	/**
	 * ID와 초기 status를 갖는 {@link MDTInstance} mock을 만든다. 상태는 {@link #setStatus}로
	 * 추후 변경할 수 있다.
	 */
	private static MDTInstance mockInstance(String id, MDTInstanceStatus status) {
		MDTInstance inst = mock(MDTInstance.class);
		when(inst.getId()).thenReturn(id);
		when(inst.getStatus()).thenReturn(status);
		return inst;
	}

	/**
	 * mock된 인스턴스의 향후 {@code getStatus()} 반환값을 갱신한다.
	 */
	private static void setStatus(MDTInstance inst, MDTInstanceStatus status) {
		when(inst.getStatus()).thenReturn(status);
	}
}
