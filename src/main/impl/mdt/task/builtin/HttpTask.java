package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.KeyedValueList;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.async.Guard;
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
public class HttpTask extends AbstractThreadedExecution<List<Parameter>> implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTask.class);

	private final String m_opServerEndpoint;
	private final String m_opId;
	private final Duration m_pollInterval;
	private final boolean m_sync;
	private final Duration m_timeout;
	
	private final KeyedValueList<String, Parameter> m_inputParameters = KeyedValueList.newInstance(Parameter::getName);
	private final KeyedValueList<String, Parameter> m_outputParameters = KeyedValueList.newInstance(Parameter::getName);
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp;
	
	public HttpTask(String serverEndpoint, String opId, Duration pollInterval, boolean sync,
					@Nullable Duration timeout) {
		Preconditions.checkArgument(serverEndpoint != null);
		Preconditions.checkArgument(pollInterval != null);
		
		m_opServerEndpoint = serverEndpoint;
		m_opId = opId;
		m_pollInterval = pollInterval;
		m_sync = sync;
		m_timeout = timeout;
	}
	
	public void addOrReplaceInputParameter(Parameter param) {
		m_inputParameters.addOrReplace(param);
	}
	
	public void addOrReplaceOutputParameter(Parameter param) {
		m_outputParameters.addOrReplace(param);
	}

	@Override
	protected List<Parameter> executeWork() throws InterruptedException, CancellationException,
													TimeoutException, Exception {
		try {
			OperationRequestBody reqBody = buildParametersBody();
			
			m_guard.runAnSignalAllOrThrow(() -> {
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
			});
			
			List<Parameter> outputValues = m_httpOp.run();
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
			return m_guard.awaitUntilAndGet(() -> m_httpOp != null, () -> m_httpOp.cancel(true));
		}
		catch ( InterruptedException e ) {
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
}
