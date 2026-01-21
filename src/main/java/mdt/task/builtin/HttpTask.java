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

import utils.DataUtils;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.client.operation.HttpOperationClient;
import mdt.client.operation.OperationRequest;
import mdt.client.operation.OperationResponse;
import mdt.model.MDTException;
import mdt.model.instance.MDTInstanceManager;
import mdt.task.AbstractMDTTask;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTask extends AbstractMDTTask implements MDTTask, CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	
	public static final String OPTION_SERVER_ENDPOINT = "endpoint";
	public static final String OPTION_OPERATION = "opId";
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp = null;
	
	public HttpTask(TaskDescriptor descriptor) {
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
				if ( m_httpOp != null ) {
					throw new IllegalStateException("Task has already started");
				}
				m_httpOp = buildHttpOperation(inputArguments, outputArguments);
			});
			
			OperationResponse resp = m_httpOp.run();
			getLogger().info("HttpTask terminates");
			
			return resp.getOutputArguments();
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
			return m_guard.awaitCondition(() -> m_httpOp != null)
							.andGet(() -> m_httpOp.cancel(true));
		}
		catch ( InterruptedException e ) {
			return false;
		}
	}
	
	private HttpOperationClient buildHttpOperation(Map<String,SubmodelElement> inputs,
													Map<String,SubmodelElement> outputs) throws IOException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		OperationRequest reqBody = new OperationRequest();
		
		String endpoint = descriptor.findOptionValue(OPTION_SERVER_ENDPOINT)
									.orElseThrow(() -> new IllegalArgumentException("serverEndpoint option is not provided"));
		String opId = descriptor.findOptionValue(OPTION_OPERATION)
								.orElseThrow(() -> new IllegalArgumentException("operationId option is not provided"));
		reqBody.setOperation(opId);

		Duration pollInterval = descriptor.findOptionValue(OPTION_POLL_INTERVAL)
											.map(this::parseDuration)
											.orElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = descriptor.findOptionValue(OPTION_TIMEOUT)
									.map(this::parseDuration)
									.orElse(null);
		
		reqBody.setInputArguments(inputs);
		reqBody.setOutputArguments(outputs);
		return HttpOperationClient.builder()
									.setHttpClient(OkHttpClientUtils.newClient())
									.setEndpoint(endpoint)
									.setRequestBody(reqBody)
									.setPollInterval(pollInterval)
									.setTimeout(timeout)
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
