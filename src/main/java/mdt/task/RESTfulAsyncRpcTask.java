package mdt.task;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.DataUtils;
import utils.KeyValue;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.func.FOption;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.rpc.restful.RESTfulAsyncRpcClient;
import utils.rpc.restful.RpcRequestMessage;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.MDTException;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class RESTfulAsyncRpcTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(RESTfulAsyncRpcTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	public static final String OPTION_BASE_URL = "baseUrl";
	public static final String OPTION_OPERATION_ENDPOINT = "endpoint";
	
	private final TaskDescriptor m_descriptor;
	private RESTfulAsyncRpcClient m_restClient = null;
	private Logger m_logger = s_logger;
	
	public RESTfulAsyncRpcTask(TaskDescriptor descriptor) {
		m_descriptor = descriptor;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}

	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
		throws InterruptedException, CancellationException, TaskException {
		try {
			Map<String,ElementValue> argValues
							= FStream.from(m_descriptor.getInputArguments())
											.mapOrThrow(arg -> KeyValue.of(arg.getId(), arg.readValue(manager)))
											.mapToKeyValue(kv -> kv)
											.toMap();
			m_restClient = buildClient(argValues);
			
			Map<String,JsonNode> outputs = m_restClient.run();
			getLogger().info(getClass().getSimpleName() + "  terminates");

			// RPC 수행 결과를 outputArguments에 반영한다.
			return FStream.from(m_descriptor.getOutputArguments())
							.mapOrThrow(arg -> {
								SubmodelElement sme = arg.getReference(manager).read();
								JsonNode jnode = outputs.get(arg.getId());
								if ( jnode != null ) {
									ElementValues.update(sme, jnode);
								}
								
								return KeyValue.of(arg.getId(), sme);
							})
							.mapToKeyValue(kv -> kv)
							.toMap();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);

			Throwables.throwIfInstanceOf(cause, MDTException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulRemoteException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulIOException.class);
			throw new TaskException(cause);
		}
		catch ( CancellationException | InterruptedException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
	
	public FOption<Duration> getPollInterval() {
		return m_descriptor.findOptionValue(OPTION_POLL_INTERVAL)
							.map(UnitUtils::parseSecondDuration);
	}
	
	public FOption<Duration> getTimeout() {
		return m_descriptor.findOptionValue(OPTION_TIMEOUT)
							.map(UnitUtils::parseSecondDuration);
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	private RESTfulAsyncRpcClient buildClient(Map<String,ElementValue> inputs) throws IOException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		String baseUrl = descriptor.findOptionValue(OPTION_BASE_URL)
									.getOrThrow(() -> new IllegalArgumentException("Option '" + OPTION_BASE_URL
																					+ "' is not provided"));
		String opEp = descriptor.findOptionValue(OPTION_OPERATION_ENDPOINT)
								.getOrThrow(() -> new IllegalArgumentException("Option '" + OPTION_OPERATION_ENDPOINT
																					+ "' is not provided"));

		Duration pollInterval = descriptor.findOptionValue(OPTION_POLL_INTERVAL)
											.map(this::parseDuration)
											.getOrElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = descriptor.findOptionValue(OPTION_TIMEOUT)
									.map(this::parseDuration)
									.getOrNull();
		
		JsonMapper mapper = MDTModelSerDe.getJsonMapper();
		Map<String,JsonNode> args = KeyValueFStream.from(inputs)
													.mapValue(smev -> {
														return (JsonNode)mapper.valueToTree(smev.toValueObject());
													})
													.toMap();
		RpcRequestMessage reqMsg = new RpcRequestMessage(args);
		return RESTfulAsyncRpcClient.builder()
									.setHttpClient(OkHttpClientUtils.newClient())
									.setBaseUrl(baseUrl)
									.setOperationEndpoint(opEp)
									.setPollInterval(pollInterval)
									.setTimeout(timeout)
									.setRequestMessage(reqMsg)
									.build();
	}
	
	private Duration parseDuration(Object seconds) {
		try {
			long millSeconds = Math.round(DataUtils.asDouble(seconds) * 1000);
			return Duration.ofMillis(millSeconds);
		}
		catch ( NumberFormatException e ) {
			return UnitUtils.parseDuration("" + seconds);
		}
	}
}
