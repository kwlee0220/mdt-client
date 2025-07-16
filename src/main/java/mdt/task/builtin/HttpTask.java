package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Throwables;
import utils.UnitUtils;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.stream.FStream;

import mdt.client.operation.HttpOperationClient;
import mdt.client.operation.OperationRequest;
import mdt.client.operation.OperationResponse;
import mdt.model.MDTException;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.variable.AbstractVariable.ElementVariable;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;
import mdt.model.sm.variable.Variable;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.StringOption;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTask extends AbstractThreadedExecution<Void> implements MDTTask, CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	
	public static final String OPTION_SERVER_ENDPOINT = "endpoint";
	public static final String OPTION_OPERATION = "opId";
//	public static final String OPTION_SYNC = "sync";
	public static final String OPTION_POLL_INTERVAL = "poll";
	public static final String OPTION_TIMEOUT = "timeout";

	private final TaskDescriptor m_descriptor;
	@GuardedBy("m_manager") private MDTInstanceManager m_manager;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private HttpOperationClient m_httpOp = null;
	
	public HttpTask(TaskDescriptor descriptor) {
		m_descriptor = descriptor;
		
		setLogger(s_logger);
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		m_manager = manager;
		
		try {
			run();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);

			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
			Throwables.throwIfInstanceOf(cause, MDTException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulRemoteException.class);
			Throwables.throwIfInstanceOf(cause, RESTfulIOException.class);
			throw new TaskException(cause);
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}

	@Override
	protected Void executeWork() throws InterruptedException, CancellationException, TimeoutException, Exception {
		Instant started = Instant.now();
		
		m_guard.runChecked(() -> {
			if ( m_httpOp != null ) {
				throw new IllegalStateException("Task has already started");
			}
			m_httpOp = buildHttpOperation();
		});
		
		OperationResponse resp = m_httpOp.run();
		if ( s_logger.isInfoEnabled() ) {
			s_logger.info("HttpTask terminates");
		}

		// CommandExecution이 정상적으로 종료된 경우:
		// CommandVariable들 중에서 output parameter의 이름과 동일한 varaible의
		// 값을 해당 parameter의 SubmodelElement을 갱신시킨다.
		updateOutputVariables(resp.getResult());

		// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
		TaskUtils.updateLastExecutionTime(m_manager, m_descriptor, started, getLogger());
		
		return null;
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
	
	
	private HttpOperationClient buildHttpOperation() throws IOException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		OperationRequest reqBody = new OperationRequest();
		
		String endpoint = descriptor.findOption(OPTION_SERVER_ENDPOINT, StringOption.class)
									.map(StringOption::getValue)
									.getOrThrow(() -> new IllegalArgumentException("serverEndpoint option is not provided"));
		String opId = descriptor.findOption(OPTION_OPERATION, StringOption.class)
								.map(StringOption::getValue)
								.getOrThrow(() -> new IllegalArgumentException("operationId option is not provided"));
		reqBody.setOperation(opId);
		
//		boolean sync = descriptor.findStringOption(OPTION_SYNC)
//									.map(DataUtils::asBoolean)
//									.getOrElse(false);
//		reqBody.setAsync(!sync);

		Duration pollInterval = descriptor.findOption(OPTION_POLL_INTERVAL)
											.map(opt -> UnitUtils.parseDuration(opt.getValue().toString()))
											.getOrElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = descriptor.findOption(OPTION_TIMEOUT)
									.map(opt -> UnitUtils.parseDuration(opt.getValue().toString()))
									.getOrNull();
		
		reqBody.setInputVariables(descriptor.getInputVariables());
		reqBody.setOutputVariables(descriptor.getOutputVariables());
		
		String json = MDTModelSerDe.MAPPER.writeValueAsString(reqBody);
		System.out.println(json);
		
		return HttpOperationClient.builder()
									.setHttpClient(OkHttpClientUtils.newClient())
									.setEndpoint(endpoint)
									.setRequestBody(reqBody)
									.setPollInterval(pollInterval)
									.setTimeout(timeout)
									.build();
	}
	
	private void updateOutputVariables(List<Variable> result) {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		Map<String,Variable> resultMap = FStream.from(result).tagKey(Variable::getName).toMap();
		
		for ( Variable outVar: descriptor.getOutputVariables() ) {
			// Output port와 동일 이름을 가진 command variable을 찾는다.
			Variable resultVar = resultMap.get(outVar.getName());
			if ( resultVar == null ) {
				continue;
			}
			if ( resultVar instanceof ReferenceVariable ) {
				// output이 reference variable인 경우에는 이미 update가 되기 때문에 무시한다.
				continue;
			}
			
			// 동일 이름을 command-variable이 검색된 경우.
			try {
				if ( resultVar instanceof ElementVariable elmVar ) {
					outVar.update(elmVar.read());
				}
				else if ( resultVar instanceof ValueVariable valVar ) {
					outVar.updateValue(valVar.readValue());
				}
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("Updated: output variable[{}]: {}", outVar.getName(), resultVar);
				}
			}
			catch ( IOException e ) {
				getLogger().error("Failed to update output variable[{}]: {}, cause={}",
									outVar.getName(), resultVar, e);
			}
		}
	}
}
