package mdt.task;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import okhttp3.OkHttpClient;

import utils.KeyValue;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.func.FOption;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.rpc.restful.RESTfulRpcClient;
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
public class RESTfulRpcTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(RESTfulRpcTask.class);

	public static final String OPTION_URL = "opUrl";
	
	private final TaskDescriptor m_descriptor;
	private RESTfulRpcClient m_restClient = null;
	private Logger m_logger = s_logger;
	
	public RESTfulRpcTask(TaskDescriptor descriptor) {
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
			
			Map<String,JsonNode> outputs = m_restClient.call();
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
		catch ( CancellationException e ) {
			throw e;
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			// 인터럽트는 TaskException으로 감싸지 않고 InterruptedException으로 전파한다.
			// (MDTTask.run() 계약) call()이 InterruptedException을 선언하지 않기 때문에 catch 절로는
			// 잡을 수 없지만, FStream.mapOrThrow()가 sneakyThrow를 사용하므로 런타임에는 그대로 올라올 수
			// 있고, RuntimeInterruptedException으로 감싸여 오는 경우도 unwrapThrowable()이 벗겨준다.
			if ( cause instanceof InterruptedException ie ) {
				throw ie;
			}
			// OkHttp는 blocking socket I/O 중의 Thread.interrupt()에 반응하지 않기 때문에, 인터럽트된
			// 호출은 인터럽트 상태가 유지된 InterruptedIOException으로 올라온다. callTimeout 만료도 같은
			// 예외를 사용하므로 인터럽트 상태로 구분한다. (HttpRESTfulClient.call() 참조)
			// Thread.interrupted()는 상태를 읽고 지우기 때문에, InterruptedException으로 변환하는
			// 이 경로에서만 호출되어야 한다.
			if ( cause instanceof InterruptedIOException && Thread.interrupted() ) {
				InterruptedException ie = new InterruptedException("task interrupted: "
																	+ getClass().getSimpleName());
				ie.initCause(cause);
				throw ie;
			}
			
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
	
	private RESTfulRpcClient buildClient(Map<String,ElementValue> inputs) throws IOException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		String url = descriptor.findOptionValue(OPTION_URL)
								.getOrThrow(() -> new IllegalArgumentException("Option '" + OPTION_URL
																				+ "' is not provided"));

		// 동기 호출에서는 단일 HTTP 응답이 원격 연산이 종료될 때까지 지연되기 때문에, OkHttp의
		// 기본 readTimeout(10초)을 그대로 두면 10초 이상 걸리는 연산은 모두 타임아웃으로 실패한다.
		// 그래서 readTimeout은 해제하고, 호출 전체의 제한 시간은 'timeout' 옵션 값을 callTimeout으로
		// 설정하여 처리한다. (Duration.ZERO는 무제한을 의미한다.)
		Duration callTimeout = getTimeout().getOrElse(Duration.ZERO);
		OkHttpClient httpClient = new OkHttpClient.Builder()
										.callTimeout(callTimeout)
										.readTimeout(Duration.ZERO)
										.build();
		
		JsonMapper mapper = MDTModelSerDe.getJsonMapper();
		Map<String,JsonNode> args = KeyValueFStream.from(inputs)
													.mapValue(smev -> {
														return (JsonNode)mapper.valueToTree(smev.toValueObject());
													})
													.toMap();
		RpcRequestMessage reqMsg = new RpcRequestMessage(args);

		return RESTfulRpcClient.builder()
								.setHttpClient(httpClient)
								.setOperationUrl(url)
								.setJsonMapper(MDTModelSerDe.getJsonMapper())
								.setInputs(args)
								.build();
	}
}
