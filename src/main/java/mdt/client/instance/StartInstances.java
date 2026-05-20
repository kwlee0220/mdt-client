package mdt.client.instance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Preconditions;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.func.Funcs;
import utils.stream.FStream;
import utils.thread.Guard;

import mdt.client.HttpMDTManager;
import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;

/**
 * 다수의 MDT 인스턴스를 비동기적으로 시작시키는 실행기.
 * <p>
 * {@link #newInstance()}로 생성한 뒤 fluent 방식으로 시작 대상 인스턴스와 동작 옵션
 * (폴링 주기, 타임아웃, 동시성, 재귀 시작 여부 등)을 설정하고,
 * {@link utils.async.AbstractThreadedExecution#run() run()}을 호출하면 지정된 동시성 수준 만큼의
 * 쓰레드가 인스턴스들을 병렬로 시작시킨다.
 * <p>
 * 주요 동작 옵션:
 * <ul>
 *   <li>{@link #pollInterval(Duration)}: 인스턴스 상태 폴링 주기.
 * 		{@code null}이면 서버 측에서 인스턴스가 시작 작업이 종료될 때까지 대기한다.</li>
 *   <li>{@link #startTimeout(Duration)}: 단일 인스턴스 시작 최대 대기 시간.</li>
 *   <li>{@link #concurrency(int)}: 동시에 시작할 수 있는 인스턴스 수.</li>
 *   <li>{@link #recursive(boolean)}: 시작된 인스턴스의 종속 인스턴스들도 재귀적으로 시작.</li>
 *   <li>{@link #statusListener(MDTInstanceStatusChangedListener)},
 *       {@link #failureListener(BiConsumer)}: 각 인스턴스의 상태 변화 및 실패 통지를 받기 위한 리스너.</li>
 * </ul>
 * <p>
 * 시작 요청 대상은 내부 대기 큐({@code m_waitingJobs})에서 꺼내져 처리되며, 동시 실행 수는
 * {@link Semaphore}로 제한된다. 재귀 모드에서는 시작에 성공한 인스턴스의 종속 인스턴스들이
 * 대기 큐에 추가되어 같은 절차로 시작된다. 최종적으로 RUNNING 상태에 도달한 인스턴스 목록이
 * {@code run()}의 결과로 반환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class StartInstances extends AbstractThreadedExecution<List<HttpMDTInstanceClient>> {
	private static final Logger s_logger = LoggerFactory.getLogger(StartInstances.class);
	
	/** 기본 상태 확인 주기 (1초). */
	private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);
	/** 기본 인스턴스 시작 제한 시간 (1분). */
	private static final Duration DEFAULT_START_TIMEOUT = Duration.ofMinutes(1);
	
	private Duration m_pollingInterval;
	private Duration m_timeout;
	private boolean m_recursive;
	private volatile Semaphore m_semaphore;
	private int m_concurrency;
	private final Deque<HttpMDTInstanceClient> m_waitingJobs;
	private volatile MDTInstanceStatusChangedListener m_statusListener;
	private volatile BiConsumer<HttpMDTInstanceClient, Exception> m_failureListener = this::printStartFailure;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final List<HttpMDTInstanceClient> m_result = new ArrayList<>();

	/**
	 * 기본값으로 {@code StartInstances}를 생성한다.
	 * <p>
	 * 기본 설정은 다음과 같다:
	 * <ul>
	 *   <li>폴링 주기: {@code null} (서버 통지 방식)</li>
	 *   <li>시작 타임아웃: {@link #DEFAULT_START_TIMEOUT} (1분)</li>
	 *   <li>재귀 시작: 비활성화</li>
	 *   <li>동시성: 1</li>
	 * </ul>
	 */
	private StartInstances() {
		m_pollingInterval = null;
		m_timeout = DEFAULT_START_TIMEOUT;
		m_recursive = false;
		m_semaphore = new Semaphore(1);
		m_concurrency = 1;
		m_waitingJobs = new ConcurrentLinkedDeque<>();

		setLogger(s_logger);
	}
	
	/**
	 * 새 {@code StartInstances} 객체를 생성한다.
	 *
	 * @return 기본 설정값으로 초기화된 새 {@code StartInstances} 객체.
	 */
	public static StartInstances newInstance() {
		return new StartInstances();
	}

	/**
	 * 인스턴스 시작 후 상태를 주기적으로 확인하는 폴링 간격을 설정한다.
	 * <p>
	 * {@code null}을 지정하면 폴링 대신 서버 측 상태 변경 통지 방식을 사용한다.
	 * {@code null}이 아닌 경우 양수 {@link Duration}이어야 한다.
	 *
	 * @param interval	상태 확인 간격. {@code null}이면 서버 통지 방식을 사용.
	 * @return	자기 자신.
	 * @throws IllegalArgumentException	{@code interval}이 0이거나 음수인 경우.
	 */
	public StartInstances pollInterval(Duration interval) {
		if ( interval != null ) {
			Preconditions.checkArgument(!interval.isNegative() && !interval.isZero(),
										"invalid interval: " + interval);
		}

		m_pollingInterval = interval;
		return this;
	}

	/**
	 * 시작할 인스턴스를 대기 큐에 추가한다.
	 *
	 * @param instance	시작할 MDT 인스턴스 클라이언트.
	 * @return	자기 자신.
	 * @throws IllegalArgumentException	{@code instance}가 {@code null}인 경우.
	 */
	public StartInstances addInstance(HttpMDTInstanceClient instance) {
		Preconditions.checkNotNullArgument(instance, "instance is null");

		m_waitingJobs.add(instance);
		return this;
	}

	/**
	 * 시작할 인스턴스 컬렉션을 대기 큐에 추가한다.
	 *
	 * @param instanceList	시작할 MDT 인스턴스 클라이언트들의 컬렉션.
	 * @return	자기 자신.
	 * @throws IllegalArgumentException	{@code instanceList}가 {@code null}인 경우.
	 */
	public StartInstances addInstanceAll(Collection<HttpMDTInstanceClient> instanceList) {
		Preconditions.checkNotNullIterableArgument(instanceList, "instance list is null");

		m_waitingJobs.addAll(instanceList);
		return this;
	}

	/**
	 * 단일 인스턴스의 시작을 기다리는 최대 시간을 설정한다.
	 * <p>
	 * 기본값은 {@link #DEFAULT_START_TIMEOUT} (1분)이다.
	 *
	 * @param timeout	시작 최대 대기 시간. 양수 {@link Duration}이어야 함.
	 * @return	자기 자신.
	 * @throws IllegalArgumentException	{@code timeout}이 {@code null}이거나, 0 또는 음수인 경우.
	 */
	public StartInstances startTimeout(Duration timeout) {
		Preconditions.checkArgument(timeout == null || (!timeout.isNegative() && !timeout.isZero()),
									"invalid timeout: " + timeout);

		m_timeout = timeout;
		return this;
	}

	/**
	 * 종속 인스턴스에 대한 재귀적 시작 여부를 설정한다.
	 * <p>
	 * {@code true}로 설정하면 시작에 성공한 인스턴스의 종속(component) 인스턴스들을 모두
	 * 조회하여 대기 큐에 추가하고, 동일한 절차로 재귀적으로 시작시킨다.
	 *
	 * @param flag	{@code true}이면 종속 인스턴스도 재귀적으로 시작.
	 * @return	자기 자신.
	 */
	public StartInstances recursive(boolean flag) {
		m_recursive = flag;
		return this;
	}

	/**
	 * 동시에 시작할 수 있는 인스턴스의 최대 수를 설정한다.
	 * <p>
	 * 기본값은 1로 한 번에 한 인스턴스만 시작된다. 내부적으로 {@link Semaphore}의 크기로
	 * 사용된다. 빌더 단계({@link #executeWork()} 진입 전)에서만 호출해야 한다.
	 *
	 * @param level	동시성 수준. 1 이상의 정수.
	 * @return	자기 자신.
	 * @throws IllegalArgumentException	{@code level}이 1 미만인 경우.
	 */
	public StartInstances concurrency(int level) {
		Preconditions.checkArgument(level > 0, "invalid concurrency level: " + level);

		m_concurrency = level;
		m_semaphore = new Semaphore(level);
		return this;
	}

	/**
	 * 인스턴스 상태 변경 시 호출될 리스너를 설정한다.
	 * <p>
	 * 각 인스턴스의 시작 작업이 종료된 후 최종 상태(예: {@link MDTInstanceStatus#RUNNING},
	 * {@link MDTInstanceStatus#FAILED})를 전달받아 처리할 수 있다.
	 *
	 * @param listener	상태 변경 리스너. {@code null}이면 통지를 받지 않음.
	 * @return	자기 자신.
	 */
	public StartInstances statusListener(MDTInstanceStatusChangedListener listener) {
		m_statusListener = listener;
		return this;
	}

	/**
	 * 인스턴스 시작 실패 시 호출될 리스너를 설정한다.
	 * <p>
	 * 별도로 지정하지 않는 경우에는 실패 원인을 표준 출력에 출력하는 기본 리스너가 사용된다.
	 *
	 * @param listener	실패 리스너. 첫 번째 인자는 실패한 인스턴스, 두 번째 인자는 원인 예외.
	 * @return	자기 자신.
	 */
	public StartInstances failureListener(BiConsumer<HttpMDTInstanceClient, Exception> listener) {
		m_failureListener = listener;
		return this;
	}

	/**
	 * 대기 큐에 등록된 모든 인스턴스를 동시성 수준에 맞춰 비동기로 시작시킨다.
	 * <p>
	 * 동작 절차:
	 * <ol>
	 *   <li>대기 큐가 빌 때까지, {@link Semaphore}로 동시 실행 수를 제한하면서 큐에서 인스턴스를
	 *       하나씩 꺼내 시작 작업을 쓰레드에 할당한다. 외부에서 주입된
	 *       {@link java.util.concurrent.Executor Executor}가 있으면 이를 사용하고, 없으면 새 쓰레드를
	 *       생성하여 실행한다.</li>
	 *   <li>큐가 빈 뒤에는 진행 중인 모든 시작 쓰레드가 종료될 때까지 대기한다.</li>
	 *   <li>재귀 모드에서 종속 인스턴스가 큐에 추가되었을 수 있으므로, 큐를 다시 확인하여
	 *       남아 있으면 같은 절차를 반복한다.</li>
	 * </ol>
	 * 최종적으로 RUNNING 상태에 도달한 인스턴스들의 목록을 반환한다.
	 *
	 * @return RUNNING 상태로 시작된 인스턴스 목록.
	 * @throws InterruptedException	대기 도중 인터럽트된 경우.
	 * @throws CancellationException	실행이 취소된 경우.
	 * @throws Exception	기타 실행 중 발생한 예외.
	 */
	@Override
	protected List<HttpMDTInstanceClient> executeWork() throws InterruptedException, CancellationException,
																Exception {
		m_guard.run(m_result::clear);
		while ( true ) {
			while ( !m_waitingJobs.isEmpty() ) {
				m_semaphore.acquire();

				HttpMDTInstanceClient inst = m_waitingJobs.removeFirst();
				try {
					if ( getExecutor() != null ) {
						getExecutor().execute(() -> startInstance(inst));
					}
					else {
						String threadName = String.format("start-instance:%s", inst.getId());
						new Thread(() -> startInstance(inst), threadName).start();
					}
				}
				catch ( Error e ) {
					// OOM 등 회복 불가한 Error는 release 후 즉시 재던진다.
					m_semaphore.release();
					throw e;
				}
				catch ( Throwable e ) {
					// 작업 제출에 실패하면 startInstance가 호출되지 못해 semaphore가 release되지 않는다.
					// 누수를 방지하기 위해 여기서 직접 release하고 실패를 통지한다.
					m_semaphore.release();
					notifyFailure(inst, Throwables.toException(Throwables.unwrapThrowable(e)));
				}
			}
			
			// start 중인 쓰레드를 대기
			m_semaphore.acquire(m_concurrency);
			m_semaphore.release(m_concurrency);
			
			// 모든 start 쓰레드가 종료된 후 waitingJob queue를 마지막으로 확인
			if ( m_waitingJobs.isEmpty() ) {
				break;
			}
		}

		return m_guard.get(() -> new ArrayList<>(m_result));
	}
	
	/**
	 * 단일 인스턴스에 대해 실제 시작 작업을 수행한다.
	 * <p>
	 * 절차:
	 * <ol>
	 *   <li>{@link HttpMDTInstanceClient#start(Duration, Duration)}을 호출하여 시작 요청을 보낸다.
	 *       이미 시작된 상태 등으로 발생하는 {@link InvalidResourceStatusException}은 무시한다.</li>
	 *   <li>재귀 모드이거나 폴링 주기가 설정된 경우, 인스턴스 상태가 {@link MDTInstanceStatus#STARTING}을
	 *       벗어날 때까지 대기한다.</li>
	 *   <li>최종 상태가 {@link MDTInstanceStatus#RUNNING}이면 결과 목록에 추가하고, 재귀 모드인 경우
	 *       종속 인스턴스들을 수집하여 대기 큐에 추가한다.</li>
	 *   <li>상태 변경 리스너에게 최종 상태를 통지한다.</li>
	 * </ol>
	 * 시작 중 발생한 예외는 {@link #notifyFailure(HttpMDTInstanceClient, Exception)}을 통해 실패
	 * 리스너에 전달되며, 어떠한 결과이든 최종적으로는 동시성 제어용 {@link Semaphore}를 반환한다.
	 *
	 * @param inst	시작할 인스턴스.
	 */
	private void startInstance(HttpMDTInstanceClient inst) {
		try {
			Instant startDue = m_timeout != null ? Instant.now().plus(m_timeout) : null;
			try {
				inst.start(m_pollingInterval, m_timeout);
			}
			catch ( InvalidResourceStatusException ignored ) { }
			
			MDTInstanceStatus status = inst.getStatus();
			if ( m_recursive || m_pollingInterval != null ) {
				Duration pollInterval = m_pollingInterval != null
										? m_pollingInterval : DEFAULT_POLLING_INTERVAL;
				status = inst.waitWhileStatus(s -> s == MDTInstanceStatus.STARTING,
											pollInterval, startDue);
				if ( status == MDTInstanceStatus.RUNNING && m_recursive ) {
					collectChildrenJobs(inst).forEach(m_waitingJobs::add);
				}
			}
			
			if ( status == MDTInstanceStatus.RUNNING ) {
				m_guard.run(() -> m_result.add(inst));
			}
			notifyStatusChanged(inst, status);
		}
		catch ( InterruptedException | TimeoutException e ) {
			notifyFailure(inst, e);
		}
		catch ( Exception e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			notifyFailure(inst, Throwables.toException(cause));
		}
		finally {
			m_semaphore.release();
		}
	}

	/**
	 * 등록된 상태 변경 리스너에 인스턴스의 상태 변경을 통지한다.
	 * <p>
	 * 리스너 호출 중 발생한 예외는 경고 로그로 남기고 무시한다.
	 *
	 * @param inst		상태가 변경된 인스턴스.
	 * @param newStatus	새 상태.
	 */
	private void notifyStatusChanged(HttpMDTInstanceClient inst, MDTInstanceStatus newStatus) {
		MDTInstanceStatusChangedListener listener = m_statusListener;
		if ( listener != null ) {
			try {
				listener.onStatusChanged(inst, newStatus);
			}
			catch ( Exception e ) {
				getLogger().warn("Failed to notify status changed: {}, cause: {}", inst.getId(),
									Throwables.unwrapThrowable(e));
			}
		}
	}
	
	/**
	 * 등록된 실패 리스너에 인스턴스 시작 실패를 통지한다.
	 * <p>
	 * 리스너 호출 중 발생한 예외는 경고 로그로 남기고 무시한다.
	 *
	 * @param inst	시작에 실패한 인스턴스.
	 * @param cause	실패 원인.
	 */
	private void notifyFailure(HttpMDTInstanceClient inst, Exception cause) {
		BiConsumer<HttpMDTInstanceClient, Exception> listener = m_failureListener;
		if ( listener != null ) {
			try {
				listener.accept(inst, cause);
			}
			catch ( Exception e ) {
				getLogger().warn("Failed to notify failure(id={}, failure={}), cause={}",
								inst.getId(), cause, Throwables.unwrapThrowable(e));
			}
		}
	}
	
	/**
	 * 기본 실패 리스너. 인스턴스 시작 실패 정보를 표준 출력에 출력한다.
	 *
	 * @param inst	시작에 실패한 인스턴스.
	 * @param cause	실패 원인.
	 */
	private void printStartFailure(HttpMDTInstanceClient inst, Exception cause) {
		System.out.println("MDTInstance failed to start: " + inst.getId() + ", cause: " + cause);
	}

	/**
	 * 주어진 인스턴스의 종속(component) 인스턴스들을 조회하여 시작 작업 목록으로 반환한다.
	 * <p>
	 * 재귀 모드에서 RUNNING 상태에 도달한 인스턴스의 후속 시작 대상을 구성할 때 사용된다.
	 *
	 * @param instance	종속 인스턴스를 조회할 부모 인스턴스.
	 * @return {@link HttpMDTInstanceClient}로 안전하게 캐스팅된 종속 인스턴스 목록.
	 */
	private List<HttpMDTInstanceClient> collectChildrenJobs(HttpMDTInstanceClient instance) {
		// 시작된 인스턴스의 종속 인스턴스들을 시작한다.
		List<MDTInstance> dependents = instance.getComponentInstanceAll();
		if ( getLogger().isInfoEnabled() ) {
			List<String> depInstIdList = Funcs.map(dependents, MDTInstance::getId);
			if ( !depInstIdList.isEmpty() ) {
				getLogger().info("Starting dependent instances: id={}, dependents={}",
									instance.getId(), depInstIdList);
			}
		}
		
		return FStream.from(dependents).castSafely(HttpMDTInstanceClient.class).toList();
	}
	
	/**
	 * {@code StartInstances}의 사용 예제 진입점.
	 * <p>
	 * 기본 설정으로 MDT 매니저에 접속한 후, {@code "innercase"} 인스턴스를 동시성 3과 재귀
	 * 모드로 시작하고, 진행 상황을 표준 출력에 출력한다.
	 *
	 * @param args	명령행 인자 (사용되지 않음).
	 * @throws Exception	연결 또는 시작 작업에서 발생한 예외.
	 */
	public static final void main(String... args) throws Exception {
		// 기본적인 설정 정보를 이용하여 MDT Manager에 접속한다.
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager =  mdt.getInstanceManager();
		
		List<HttpMDTInstanceClient> instList = manager.getInstanceAll();
		var innercase = manager.getInstance("innercase");
		
		List<HttpMDTInstanceClient> result = StartInstances.newInstance()
													.addInstance(innercase)
													.recursive(true)
													.pollInterval(null)
													.concurrency(3)
													.statusListener(StartInstances::printInstanceStatus)
													.run();
		System.out.println("StartInstances result: " + result);
	}
	
	/**
	 * {@link #main(String...)} 예제에서 사용되는 상태 변경 리스너. 인스턴스의 현재 상태를
	 * 사람이 읽기 쉬운 메시지로 표준 출력에 출력한다.
	 *
	 * @param inst		상태가 변경된 인스턴스.
	 * @param status	새 상태.
	 */
	private static void printInstanceStatus(MDTInstance inst, MDTInstanceStatus status) {
		switch ( status ) {
			case STARTING:
				System.out.println("MDTInstance starting: " + inst.getId());
				break;
			case RUNNING:
				System.out.println("MDTInstance started: " + inst.getId());
				break;
			case FAILED:
				System.out.println("MDTInstance failed to start: " + inst.getId());
				break;
			default:
				System.out.println("MDTInstance failed to start: " + inst.getId()
									+ ", unexpected state: " + status);
				break;
		}
	}
}
