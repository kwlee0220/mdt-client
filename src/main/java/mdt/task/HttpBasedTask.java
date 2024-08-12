package mdt.task;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.Throwables;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.client.SSLUtils;
import mdt.client.operation.HttpOperationClient;
import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpBasedTask implements MDTTask {
	@SuppressWarnings("unused")
	private static final Logger s_logger = LoggerFactory.getLogger(HttpBasedTask.class);
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);
	private static final JsonMapper MAPPER = new JsonMapper();
	
	private String m_endpoint;
	private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
	private Duration m_timeout;

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) {
	}
	
	public void setServerEndpoint(String endpoint) {
		m_endpoint = endpoint;
	}
	
	public void setPollInterval(Duration interval) {
		m_pollInterval = interval;
	}
	
	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	@Override
	public void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		try {
			Map<String,Object> parameters = Maps.newHashMap();
			FStream.from(inputPorts).mapValue(Port::getAsJsonObject).forEach(parameters::put);
			FStream.from(inoutPorts).mapValue(Port::getAsJsonObject).forEach(parameters::put);
			FStream.from(outputPorts).mapValue(Port::getAsJsonObject).forEach(parameters::put);
			FStream.from(options).forEach(parameters::put);
			String paramsJsonStr = AASUtils.writeJson(parameters);
			
			HttpOperationClient op = HttpOperationClient.builder()
											.setHttpClient(SSLUtils.newTrustAllOkHttpClientBuilder().build())
											.setEndpoint(m_endpoint)
											.setRequestBodyJson(paramsJsonStr)
											.setPollInterval(m_pollInterval)
											.setTimeout(m_timeout)
											.build();
			JsonNode result = op.run();
			
			Map<String,Port> resultPorts = Maps.newHashMap(outputPorts);
			resultPorts.putAll(inoutPorts);
			
			if ( result != null ) {
				ObjectNode top = (ObjectNode)result;
				FStream.from(top.fields())
						.forEach(ent -> update(resultPorts.get(ent.getKey()), ent.getValue()));
			}
		}
		catch ( Exception e ) {
			Throwables.throwIfInstanceOf(e, TimeoutException.class);
			Throwables.throwIfInstanceOf(e, CancellationException.class);
			Throwables.throwIfInstanceOf(e, InterruptedException.class);
			
			throw new ExecutionException(e);
		}
	}
	
	private void update(Port port, JsonNode valueNode) {
		if ( valueNode.isValueNode() ) {
			try {
				String json = MAPPER.writeValueAsString(valueNode);
				port.setJson(json);
			}
			catch ( JsonProcessingException e ) {
				throw new InternalException("Failed to write JSON, cause=" + e);
			}
		}
		else {
			SubmodelElement result = AASUtils.readJson(valueNode, SubmodelElement.class);
			port.set(result);
		}
	}
	
	public static class Command extends MDTTaskCommand<HttpBasedTask> {
		@Option(names={"--task_server"}, paramLabel="endpoint", description="The endpoint for the HTTP-based task")
		private String m_taskServerEndpoint;

		private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
		@Option(names={"--poll"}, paramLabel="duration",
				description="Status polling interval (e.g. \"5s\", \"500ms\"")
		public void setPollInterval(String intervalStr) {
			m_pollInterval = UnitUtils.parseDuration(intervalStr);
		}

		@Override
		protected HttpBasedTask newTask() {
			HttpBasedTask task = new HttpBasedTask();
			task.setMDTInstanceManager(m_manager);
			task.setServerEndpoint(m_taskServerEndpoint);
			task.setPollInterval(m_pollInterval);
			task.setTimeout(m_timeout);
			
			return task;
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
