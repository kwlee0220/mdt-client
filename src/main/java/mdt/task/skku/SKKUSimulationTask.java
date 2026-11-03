package mdt.task.skku;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.async.AsyncState;
import utils.thread.Guard;

import mdt.client.operation.HttpSimulationClient;
import mdt.client.operation.OperationStatus;
import mdt.client.operation.OperationStatusResponse;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.SubmodelReference;
import mdt.model.sm.simulation.Simulation;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;


/**
 * SKKU 시뮬레이터에 Simulation Submodel의 시뮬레이션 수행을 요청하는 {@link MDTTask} 구현체.
 * <p>
 * 대상 Simulation Submodel의 시뮬레이터 endpoint({@link Simulation#IDSHORT_PATH_ENDPOINT})로
 * {@link HttpSimulationClient}를 만들어 시뮬레이션을 시작하고, 종료될 때까지 주기적으로 상태를 확인한다.
 * 시뮬레이션 결과는 시뮬레이터가 Submodel에 직접 기록하므로 태스크는 결과 값을 반환하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SKKUSimulationTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(SKKUSimulationTask.class);
	static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

	private SubmodelReference m_simRef;
	private Duration m_timeout = DEFAULT_TIMEOUT;
	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;

	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private AsyncState m_status = AsyncState.NOT_STARTED;
	
	/**
	 * 기본 polling 주기(3초)와 제한 시간(5분)을 갖는 태스크를 만든다.
	 */
	protected SKKUSimulationTask() {
	}

	/**
	 * 빈 태스크 기술자를 반환한다. 호출할 때마다 새 객체를 만든다.
	 *
	 * @return	빈 태스크 기술자.
	 */
	@Override
	public TaskDescriptor getTaskDescriptor() {
		return new TaskDescriptor();
	}
	
	/**
	 * 시뮬레이션 대상 Simulation Submodel의 참조를 설정한다.
	 * <p>
	 * 참조는 활성화된 상태여야 하고, 가리키는 서비스는 {@link HttpSubmodelServiceClient}여야 한다.
	 *
	 * @param ref	Simulation Submodel 참조.
	 */
	public void setSimulationSubmodelReference(SubmodelReference ref) {
		m_simRef = ref;
	}
	
	/**
	 * 시뮬레이션 상태 확인 주기를 설정한다. 기본값은 3초이다.
	 *
	 * @param interval	상태 확인 주기.
	 */
	public void setPollInterval(Duration interval) {
		m_pollInterval = interval;
	}
	
	/**
	 * 시뮬레이션 수행 제한 시간을 설정한다. 기본값은 5분이다.
	 *
	 * @param timeout	제한 시간. {@code null}이면 제한 없음.
	 */
	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	/**
	 * 시뮬레이션을 시작하고 종료될 때까지 대기한다.
	 * <p>
	 * 제한 시간을 넘기면 시뮬레이션 취소를 요청한 뒤 {@link TimeoutException}을 원인으로 하는
	 * {@link TaskException}을 던진다. 어떻게 끝나든 태스크 상태를 최종 상태(완료/실패/취소)로 바꾸고
	 * {@link #cancel()}로 대기 중인 스레드를 깨운다.
	 *
	 * @param manager	MDT 인스턴스 관리자 (사용하지 않음).
	 * @return	항상 빈 맵.
	 * @throws InterruptedException	상태 확인 대기 중 인터럽트된 경우.
	 * @throws CancellationException	{@link #cancel()}로 취소되었거나(시작 전 취소 포함) 시뮬레이션이 취소 상태로
	 * 									종료된 경우.
	 * @throws TaskException	시뮬레이터 endpoint가 Submodel에 없거나, 시뮬레이션이 실패했거나 제한 시간을 초과한 경우.
	 * @throws IllegalArgumentException	대상 Submodel이 Simulation Submodel이 아닌 경우.
	 */
	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
		throws InterruptedException, CancellationException, TaskException {
		m_guard.lock();
		try {
			if ( m_status == AsyncState.CANCELLED ) {
				throw new CancellationException("cancelled before start: ref=" + m_simRef);
			}
			m_status = AsyncState.STARTING;
		}
		finally {
			m_guard.unlock();
		}
		
		try {
			Map<String, SubmodelElement> result = runSimulation();
			finish(AsyncState.COMPLETED);
			return result;
		}
		catch ( CancellationException e ) {
			finish(AsyncState.CANCELLED);
			throw e;
		}
		catch ( Throwable e ) {
			finish(AsyncState.FAILED);
			throw e;
		}
	}
	
	// 최종 상태를 기록하고, cancel()에서 상태 변화를 기다리는 스레드를 깨운다.
	private void finish(AsyncState state) {
		m_guard.lock();
		try {
			m_status = state;
			m_guard.signalAll();
		}
		finally {
			m_guard.unlock();
		}
	}
	
	private Map<String, SubmodelElement> runSimulation()
		throws InterruptedException, CancellationException, TaskException {
		HttpSubmodelServiceClient svc = (HttpSubmodelServiceClient)m_simRef.get();
		
		Submodel simulation = svc.getSubmodel();
		if ( !Simulation.SEMANTIC_ID_REFERENCE.equals(simulation.getSemanticId()) ) {
			String msg = String.format("The target Submodel is not for a Simulation: ref=%s", m_simRef);
			throw new IllegalArgumentException(msg);
		}
		
		String simulatorEndpoint = SubmodelUtils.getPropertyValueByPath(simulation,
																Simulation.IDSHORT_PATH_ENDPOINT,
																String.class);
		if ( simulatorEndpoint == null || simulatorEndpoint.trim().length() == 0 ) {
			throw new TaskException("Simulator Endpoint is missing: submodel-id=" + simulation.getId());
		}
		
		HttpSimulationClient client = new HttpSimulationClient(svc.getHttpClient(), simulatorEndpoint);
		client.setLogger(s_logger);
		
		Instant started = Instant.now();
		String simulationSubmodelEndpoint = svc.getEndpoint();
		OperationStatusResponse<Void> resp = client.startSimulationWithEndpoint(simulationSubmodelEndpoint);
		
		m_guard.lock();
		try {
			m_status = AsyncState.RUNNING;
			m_guard.signalAll();		// STARTING 상태에서 기다리는 cancel()을 깨운다.
		}
		finally {
			m_guard.unlock();
		}
		
		String location = resp.getOperationLocation();
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
			
			m_guard.lock();
			try {
				if ( m_status == AsyncState.CANCELLING ) {
					resp = client.cancelSimulation(location);
					if ( resp.getStatus() == OperationStatus.CANCELLED ) {
						m_status = AsyncState.CANCELLED;
						m_guard.signalAll();
						throw new CancellationException(resp.getMessage());
					}
				}
			}
			finally {
				m_guard.unlock();
			}
			
			resp = client.statusSimulation(location);
			if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					client.cancelSimulation(location);
					m_guard.run(() -> m_status = AsyncState.FAILED);
					
					throw new TaskException(new TimeoutException("Timeout expired: " + m_timeout));
				}
			}
		}
		
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return Map.of();
			case FAILED:
				throw new TaskException(new Exception(resp.getMessage()));
			case CANCELLED:
				throw new CancellationException(resp.getMessage());
			default:
				throw new AssertionError();
		}
	}

	/**
	 * 수행 중인 시뮬레이션의 취소를 요청하고, 취소가 확정될 때까지 대기한다.
	 * <p>
	 * 태스크가 아직 시작되지 않았으면 바로 취소 상태로 만들고, 이후의 {@link #run(MDTInstanceManager)}은
	 * {@link CancellationException}을 던진다. 시뮬레이션 시작을 요청하는 중이면 그 결과를 기다린다.
	 * 실제 취소 요청은 {@link #run(MDTInstanceManager)}을 수행 중인 스레드가 다음 상태 확인 시점에 보낸다.
	 *
	 * @return	취소된 경우 {@code true}, 이미 종료되었거나 취소되지 않고 끝났거나 대기 중 오류가 발생한 경우
	 * 			{@code false}.
	 */
	@Override
	public boolean cancel() {
		m_guard.lock();
		try {
			if ( m_status == AsyncState.NOT_STARTED ) {
				m_status = AsyncState.CANCELLED;
				m_guard.signalAll();
				return true;
			}
			while ( m_status == AsyncState.STARTING ) {
				m_guard.awaitSignal();
			}
			if ( m_status == AsyncState.RUNNING ) {
				m_status = AsyncState.CANCELLING;
				
				while ( m_status == AsyncState.CANCELLING ) {
					m_guard.awaitSignal();
				}
				return m_status == AsyncState.CANCELLED;
			}
			return false;
		}
		catch ( Exception e ) {
			return false;
		}
		finally {
			m_guard.unlock();
		}
	}
}
