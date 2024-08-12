package mdt.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.operation.HttpSimulationClient;
import mdt.client.operation.OperationStatus;
import mdt.client.operation.OperationStatusResponse;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.ksx9101.simulation.Simulation;
import mdt.model.SubmodelUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.SubmodelReference;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SKKUSimulationTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(SKKUSimulationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

	private HttpMDTInstanceManagerClient m_manager;
	private SubmodelReference m_simRef;
	private Duration m_timeout = DEFAULT_TIMEOUT;
	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) {
		m_manager = (HttpMDTInstanceManagerClient)manager;
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
	public void run(Map<String, Port> inputPorts, Map<String, Port> inoutPorts,
						Map<String, Port> outputPorts, Map<String, String> options) 
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		HttpSubmodelServiceClient svc = (HttpSubmodelServiceClient)m_simRef.get();
		
		Submodel simulation = svc.getSubmodel();
		if ( !Simulation.SEMANTIC_ID.equals(simulation.getSemanticId()) ) {
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
		String simulationSubmodelEndpoint = svc.getUrl();
		OperationStatusResponse<Void> resp = client.startSimulationWithEndpoint(simulationSubmodelEndpoint);
		
		String location = resp.getOperationLocation();
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
			
			resp = client.statusSimulation(location);
			if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					client.cancelSimulation(location);
					
					throw new TimeoutException("Timeout expired: " + m_timeout);
				}
			}
		}
		
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return;
			case FAILED:
				throw new ExecutionException(new Exception(resp.getMessage()));
			case CANCELLED:
				throw new CancellationException(resp.getMessage());
			default:
				throw new AssertionError();
		}
	}
	
	public static class Command extends MDTTaskCommand<SKKUSimulationTask> {
		@Option(names={"--simulation"}, paramLabel="reference",
				description="the reference to Simulation Submodel")
		private String m_simSubmodelRefString;

		private Duration m_timeout = DEFAULT_TIMEOUT;
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
		protected SKKUSimulationTask newTask() {
			SKKUSimulationTask task = new SKKUSimulationTask();
			task.setMDTInstanceManager(m_manager);
			
			SubmodelReference simRef = SubmodelReference.parseString(m_manager, m_simSubmodelRefString);
			task.setSimulationSubmodelReference(simRef);
			task.setPollInterval(m_pollInterval);
			task.setTimeout(m_timeout);
			
			return task;
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
