package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.RuntimeInterruptedException;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.async.Guard;
import utils.async.GuardedRunnable;
import utils.async.GuardedSupplier;
import utils.func.FOption;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.stream.FStream;

import mdt.client.operation.HttpOperationClient;
import mdt.client.operation.OperationRequestBody;
import mdt.model.AASUtils;
import mdt.model.MDTException;
import mdt.model.instance.MDTInstanceManager;
import mdt.task.MDTTask;
import mdt.task.Parameter;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTask extends AbstractThreadedExecution<Map<String,SubmodelElement>> implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	private final String m_opServerEndpoint;
	private final String m_opId;
	private final List<Parameter> m_inputParameters;
	private final List<Parameter> m_outputParameters;
	private final Duration m_pollInterval;
	private final boolean m_sync;
	private final Duration m_timeout;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp;
	
	private HttpTask(Builder builder) {
		Preconditions.checkArgument(builder.m_opServerEndpoint != null);
		Preconditions.checkArgument(builder.m_opId != null);
		
		m_opServerEndpoint = builder.m_opServerEndpoint;
		m_opId = builder.m_opId;
		m_inputParameters = builder.m_inputParameters;
		m_outputParameters = builder.m_outputParameters;
		m_pollInterval = FOption.getOrElse(builder.m_pollInterval, DEFAULT_POLL_INTERVAL);
		m_sync = builder.m_sync;
		m_timeout = builder.m_timeout;
	}

	@Override
	protected Map<String,SubmodelElement> executeWork() throws InterruptedException, CancellationException,
													TimeoutException, Exception {
		try {
			OperationRequestBody reqBody = buildParametersBody();
			
			GuardedRunnable.from(m_guard, () -> {
				String encodedOpId = AASUtils.encodeBase64UrlSafe(m_opId);
				String syncStr = (m_sync) ? "sync" : "async";
				String startUrl = String.format("%s/operations/%s/%s", m_opServerEndpoint, encodedOpId, syncStr);
				m_httpOp = HttpOperationClient.builder()
												.setHttpClient(OkHttpClientUtils.newClient())
												.setEndpoint(m_opServerEndpoint)
												.setStartUrl(startUrl)
												.setRequestBody(reqBody)
												.setPollInterval(m_pollInterval)
												.setTimeout(m_timeout)
												.build();
			}).run();
			
			Map<String,SubmodelElement> outputValues = m_httpOp.run();
			if ( s_logger.isInfoEnabled() ) {
				s_logger.info("HttpTask terminates");
			}
			
			return outputValues;
		}
		catch ( InterruptedException | CancellationException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
			Throwables.throwIfInstanceOf(cause, MDTException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulRemoteException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulIOException.class);
			throw new TaskException(cause);
		}
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			run();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
			Throwables.throwIfInstanceOf(cause, TaskException.class);
			throw new TaskException(cause);
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}

	@Override
	public boolean cancel() {
		try {
			return GuardedSupplier.from(m_guard, () -> m_httpOp.cancel(true))
					                .preCondition(() -> m_httpOp != null)
									.get();
		}
		catch ( RuntimeInterruptedException e ) {
			return false;
		}
	}
	
	private OperationRequestBody buildParametersBody() throws IOException	{
		OperationRequestBody body = new OperationRequestBody();
		
		List<Parameter> parameters = FStream.from(m_inputParameters)
											.concatWith(FStream.from(m_outputParameters))
											.toList();
		body.setParameters(parameters);
		
		Set<String> outputNames = FStream.from(m_outputParameters)
										.map(Parameter::getName)
										.toSet();
		body.setOutputNames(outputNames);
		
		return body;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_opServerEndpoint;
		private String m_opId;
		private Duration m_pollInterval;
		private boolean m_sync = false;
		@Nullable private Duration m_timeout;
		private KeyedValueList<String, Parameter> m_inputParameters = KeyedValueList.newInstance(Parameter::getName);
		private KeyedValueList<String, Parameter> m_outputParameters = KeyedValueList.newInstance(Parameter::getName);
		
		public HttpTask build() {
			return new HttpTask(this);
		}
		
		public Builder serverEndpoint(String endpoint) {
			m_opServerEndpoint = endpoint;
			return this;
		}
		
		public Builder operationId(String opId) {
			m_opId = opId;
			return this;
		}
		
		public Builder addInputParameter(Parameter param) {
			m_inputParameters.add(param);
			return this;
		}
		
		public Builder addOutputParameter(Parameter param) {
			m_outputParameters.add(param);
			return this;
		}
		
		public Builder pollInterval(Duration pollInterval) {
			m_pollInterval = pollInterval;
			return this;
		}
		
		public Builder timeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		public Builder sync(boolean flag) {
			m_sync = flag;
			return this;
		}
	}
}
