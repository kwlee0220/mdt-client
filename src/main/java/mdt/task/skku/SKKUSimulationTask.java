package mdt.task.skku;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.async.AsyncState;
import utils.async.Guard;

import mdt.cli.AbstractMDTCommand;
import mdt.client.operation.HttpSimulationClient;
import mdt.client.operation.OperationStatus;
import mdt.client.operation.OperationStatusResponse;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.SubmodelReference;
import mdt.model.sm.simulation.Simulation;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SKKUSimulationTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(SKKUSimulationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

	private SubmodelReference m_simRef;
	private Duration m_timeout = DEFAULT_TIMEOUT;
	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;

	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private AsyncState m_status = AsyncState.NOT_STARTED;
	
	protected SKKUSimulationTask() {
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return new TaskDescriptor();
	}
	
	public void setSimulationSubmodelReference(SubmodelReference ref) {
		m_simRef = ref;
	}
	
	public void setPollInterval(Duration interval) {
		m_pollInterval = interval;
	}
	
	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		HttpSubmodelServiceClient svc = (HttpSubmodelServiceClient)m_simRef.get();
		
		Submodel simulation = svc.getSubmodel();
		if ( !Simulation.SEMANTIC_ID_REFERENCE.equals(simulation.getSemanticId()) ) {
			String msg = String.format("The target Submode is not for a Simulation: ref=", m_simRef);
			throw new IllegalArgumentException(msg);
		}
		
		String simulatorEndpoint = SubmodelUtils.getPropertyValueByPath(simulation,
																Simulation.IDSHORT_PATH_ENDPOINT,
																String.class);
		if ( simulatorEndpoint == null || simulatorEndpoint.trim().length() == 0 ) {
			System.err.printf("Simulator Endpoint is missing: submodel-id=%s%n", simulation.getId());
			System.exit(-1);
		}
		
		HttpSimulationClient client = new HttpSimulationClient(svc.getHttpClient(), simulatorEndpoint);
		client.setLogger(s_logger);
		
		Instant started = Instant.now();
		String simulationSubmodelEndpoint = svc.getEndpoint();
		OperationStatusResponse<Void> resp = client.startSimulationWithEndpoint(simulationSubmodelEndpoint);
		
		m_guard.run(() -> m_status = AsyncState.RUNNING);
		
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
					
					throw new TimeoutException("Timeout expired: " + m_timeout);
				}
			}
		}
		
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return;
			case FAILED:
				throw new TaskException(new Exception(resp.getMessage()));
			case CANCELLED:
				throw new CancellationException(resp.getMessage());
			default:
				throw new AssertionError();
		}
	}

	@Override
	public boolean cancel() {
		m_guard.lock();
		try {
			while ( m_status == AsyncState.NOT_STARTED ) {
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

	@picocli.CommandLine.Command(name = "skku", description = "SKKU Simulation")
	public static class Command extends AbstractMDTCommand {
		@Option(names={"--simulation"}, paramLabel="reference",
				description="the reference to Simulation Submodel")
		private String m_simSubmodelRefString;
		
		protected Duration m_timeout = null;
		@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
		public void setTimeout(String toStr) {
			m_timeout = UnitUtils.parseDuration(toStr);
		}

		private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;
		@Option(names={"--pollInterval"}, paramLabel="duration",
				description="Status polling interval (e.g. \"1s\", \"500ms\"")
		public void setPollInterval(String intvStr) {
			m_pollInterval = UnitUtils.parseDuration(intvStr);
		}

		@Override
		protected void run(MDTManager mdt) throws Exception {
			MDTInstanceManager manager = mdt.getInstanceManager();
			
			SKKUSimulationTask task = new SKKUSimulationTask();
			
			DefaultSubmodelReference simRef = DefaultSubmodelReference.parseStringExpr(m_simSubmodelRefString);
			simRef.activate(manager);
			
			task.setSimulationSubmodelReference(simRef);
			task.setPollInterval(m_pollInterval);
			task.setTimeout(m_timeout);
			
			task.run(manager);
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
