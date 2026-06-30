package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.DataUtils;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.CancellableWork;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.rpc.restful.RESTfulAsyncRpcClient;
import utils.rpc.restful.RpcRequestMessage;
import utils.stream.KeyValueFStream;
import utils.thread.Guard;

import mdt.model.MDTException;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.task.AbstractMDTTask;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class RESTfulTask extends AbstractMDTTask implements MDTTask, CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(RESTfulTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	
	public static final String OPTION_BASE_URL = "baseUrl";
	public static final String OPTION_OPERATION_ENDPOINT = "endpoint";
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private RESTfulAsyncRpcClient m_restClient = null;
	
	public RESTfulTask(TaskDescriptor descriptor) {
		super(descriptor);
		
		setLogger(s_logger);
	}

	@Override
	protected Map<String,SubmodelElement> invoke(MDTInstanceManager manager,
												Map<String,SubmodelElement> inputArguments,
												Map<String,SubmodelElement> outputArguments)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			m_guard.runChecked(() -> {
				if ( m_restClient != null ) {
					throw new IllegalStateException("Task has already started");
				}
				m_restClient = buildClient(inputArguments);
			});
			
			Map<String,JsonNode> outputs = m_restClient.run();
			getLogger().info(getClass().getSimpleName() + "  terminates");

			// RPC 수행 결과를 outputArguments에 반영한다.
			KeyValueFStream.from(outputs)
							.match(outputArguments)
							.forEachOrThrow((name, match) -> {
								ElementValues.update(match._2, match._1);
							});
			return outputArguments;
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);

			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
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
		return cancelWork();
	}

	@Override
	public boolean cancelWork() {
		try {
			return m_guard.awaitCondition(() -> m_restClient != null)
							.andGet(() -> m_restClient.cancel(true));
		}
		catch ( InterruptedException e ) {
			return false;
		}
	}
	
	private RESTfulAsyncRpcClient buildClient(Map<String,SubmodelElement> inputs) throws IOException {
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
													.mapValue(sme -> {
														ElementValue smev = ElementValues.getValue(sme);
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
