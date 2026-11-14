package mdt.task;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.KeyValue;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.func.FOption;
import utils.func.Nullables;
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
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 * {@link RESTfulAsyncRpcClient}로 원격 연산을 비동기 RPC로 호출하는 {@link MDTTask} 구현체.
 * <p>
 * 태스크 기술자의 입력 인자 값을 읽어 RPC 요청의 입력으로 보내고, 원격 연산이 종료될 때까지 세션 상태를
 * 주기적으로 확인(polling)한다. 연산이 완료되면 RPC 출력 중 기술자의 출력 인자와 이름이 같은 값을
 * 해당 인자가 가리키는 요소의 현재 값에 반영하여 반환한다. 이때 반영된 값을 요소에 다시 기록하지는 않는다.
 * <p>
 * 사용하는 기술자 옵션은 다음과 같다.
 * <ul>
 * 	<li>{@value #OPTION_BASE_URL}: RPC 서버의 base URL (필수).</li>
 * 	<li>{@value #OPTION_OPERATION_ENDPOINT}: 호출할 연산의 endpoint (필수).</li>
 * 	<li>{@value TaskDescriptor#OPTION_POLL_INTERVAL}: 세션 상태 확인 주기 (선택, 기본 3초).</li>
 * 	<li>{@value TaskDescriptor#OPTION_TIMEOUT}: 전체 제한 시간 (선택).</li>
 * </ul>
 * 숫자 옵션 값은 초 단위로, 그 밖의 값은 duration 문자열로 해석한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RESTfulAsyncRpcTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(RESTfulAsyncRpcTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	/** RPC 서버의 base URL을 지정하는 옵션 이름. */
	public static final String OPTION_BASE_URL = "baseUrl";
	/** 호출할 연산의 endpoint를 지정하는 옵션 이름. */
	public static final String OPTION_OPERATION_ENDPOINT = "endpoint";
	
	private final TaskDescriptor m_descriptor;
	private RESTfulAsyncRpcClient m_restClient = null;
	private Logger m_logger = s_logger;
	
	/**
	 * 주어진 태스크 기술자로 태스크를 만든다.
	 *
	 * @param descriptor	입출력 인자와 옵션을 담은 태스크 기술자.
	 */
	public RESTfulAsyncRpcTask(TaskDescriptor descriptor) {
		m_descriptor = descriptor;
	}

	/**
	 * 생성자로 전달된 태스크 기술자를 반환한다.
	 *
	 * @return	태스크 기술자.
	 */
	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}

	/**
	 * 입력 인자 값을 RPC 요청으로 보내 원격 연산을 시작하고, 종료될 때까지 대기한다.
	 *
	 * @param manager	인자의 참조를 활성화할 MDT 인스턴스 관리자.
	 * @return	출력 인자 이름을 키로, RPC 출력 값이 반영된 요소를 값으로 하는 맵.
	 * @throws InterruptedException	대기 중 인터럽트된 경우.
	 * @throws CancellationException	원격 연산이 취소된 경우.
	 * @throws TaskException	입력 인자에 task-output 인자가 있거나, 필수 옵션이 없거나, 원격 연산이 실패하거나
	 * 						제한 시간을 초과한 경우. 단, 실패 원인이 {@link MDTException},
	 * 						{@link RESTfulRemoteException}, {@link RESTfulIOException}이면 감싸지 않고
	 * 						그대로 던진다.
	 */
	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
		throws InterruptedException, CancellationException, TaskException {
		try {
			// task-output 인자는 읽을 수 없으므로 TaskException으로 거부한다(TaskUtils.readArgumentValue).
			Map<String,ElementValue> argValues = new LinkedHashMap<>();
			for ( ArgumentSpec arg: m_descriptor.getInputArguments() ) {
				argValues.put(arg.getId(), TaskUtils.readArgumentValue(arg, manager));
			}
			m_restClient = buildClient(argValues);
			
			Map<String,JsonNode> outputs = m_restClient.run();
			getLogger().info(getClass().getSimpleName() + "  terminates");

			// RPC 수행 결과를 outputArguments에 반영한다.
			return FStream.from(m_descriptor.getOutputArguments())
							.mapOrThrow(arg -> {
								SubmodelElement sme = arg.resolveReference(manager).read();
								JsonNode jnode = outputs.get(arg.getId());
								if ( jnode != null ) {
									ElementValues.updateFromValueJson(sme, jnode);
								}
								
								return KeyValue.of(arg.getId(), sme);
							})
							.mapToKeyValue(kv -> kv)
							.toMap();
		}
		catch ( TaskException e ) {
			throw e;
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

	/**
	 * 취소를 지원하지 않는다. 수행 중인 원격 호출도 중단하지 않는다.
	 *
	 * @return	항상 {@code false}.
	 */
	@Override
	public boolean cancel() {
		return false;
	}
	
	/**
	 * 기술자의 {@value TaskDescriptor#OPTION_POLL_INTERVAL} 옵션 값을 초 단위 duration으로 해석해 반환한다.
	 * <p>
	 * 접미사 없는 숫자는 초 단위로, 그 밖의 값은 duration 문자열로 해석한다({@link UnitUtils#parseSecondDuration}).
	 * 실제 호출에도 이 값이 쓰인다.
	 *
	 * @return	polling 주기. 옵션이 없으면 {@link FOption#empty()}.
	 * @throws IllegalArgumentException	옵션 값을 duration으로 해석할 수 없는 경우.
	 */
	public FOption<Duration> getPollInterval() {
		return m_descriptor.findOptionValue(TaskDescriptor.OPTION_POLL_INTERVAL)
							.map(UnitUtils::parseSecondDuration);
	}
	
	/**
	 * 기술자의 {@value TaskDescriptor#OPTION_TIMEOUT} 옵션 값을 초 단위 duration으로 해석해 반환한다.
	 *
	 * @return	제한 시간. 옵션이 없으면 {@link FOption#empty()}.
	 * @throws IllegalArgumentException	옵션 값을 duration으로 해석할 수 없는 경우.
	 */
	public FOption<Duration> getTimeout() {
		return m_descriptor.findOptionValue(TaskDescriptor.OPTION_TIMEOUT)
							.map(UnitUtils::parseSecondDuration);
	}

	@Override
	public Logger getLogger() {
		return Nullables.orElse(m_logger, s_logger);
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

		Duration pollInterval = getPollInterval().getOrElse(DEFAULT_POLL_INTERVAL);
		Duration timeout = getTimeout().getOrNull();
		
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
}
