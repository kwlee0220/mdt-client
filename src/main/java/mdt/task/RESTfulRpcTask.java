package mdt.task;

import java.io.IOException;
import java.io.InterruptedIOException;
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

import okhttp3.OkHttpClient;

import utils.KeyValue;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.func.FOption;
import utils.func.Nullables;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.rpc.restful.RESTfulRpcClient;
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
 * {@link RESTfulRpcClient}로 원격 연산을 동기 RPC로 호출하는 {@link MDTTask} 구현체.
 * <p>
 * 태스크 기술자의 입력 인자 값을 읽어 하나의 HTTP 요청으로 보내고, 원격 연산이 끝나 응답이 올 때까지
 * 대기한다. 응답의 출력 중 기술자의 출력 인자와 이름이 같은 값을 해당 인자가 가리키는 요소의 현재 값에
 * 반영하여 반환한다. 이때 반영된 값을 요소에 다시 기록하지는 않는다.
 * <p>
 * 사용하는 기술자 옵션은 {@value #OPTION_URL}(연산 URL, 필수)과 {@value MDTTask#OPTION_TIMEOUT}(호출 전체
 * 제한 시간, 초 단위, 선택)이다. 제한 시간이 없으면 응답을 무제한 기다린다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RESTfulRpcTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(RESTfulRpcTask.class);

	/** 호출할 연산의 URL을 지정하는 옵션 이름. */
	public static final String OPTION_URL = "opUrl";
	
	private final TaskDescriptor m_descriptor;
	private RESTfulRpcClient m_restClient = null;
	private Logger m_logger = s_logger;
	
	/**
	 * 주어진 태스크 기술자로 태스크를 만든다.
	 *
	 * @param descriptor	입출력 인자와 옵션을 담은 태스크 기술자.
	 */
	public RESTfulRpcTask(TaskDescriptor descriptor) {
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
	 * 입력 인자 값을 RPC 요청으로 보내고 원격 연산의 응답이 올 때까지 대기한다.
	 *
	 * @param manager	인자의 참조를 활성화할 MDT 인스턴스 관리자.
	 * @return	출력 인자 이름을 키로, RPC 출력 값이 반영된 요소를 값으로 하는 맵.
	 * @throws InterruptedException	대기 중 인터럽트된 경우. 진행 중인 HTTP 호출이 인터럽트로 중단된 경우도
	 * 						포함한다.
	 * @throws CancellationException	원격 연산이 취소된 경우.
	 * @throws TaskException	입력 인자에 task-output 인자가 있거나, {@value #OPTION_URL} 옵션이 없거나,
	 * 						원격 연산이 실패하거나 제한 시간을 초과한 경우. 단, 실패 원인이 {@link MDTException},
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
			
			Map<String,JsonNode> outputs = m_restClient.call();
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
	 * 기술자의 {@value #OPTION_POLL_INTERVAL} 옵션 값을 초 단위 duration으로 해석해 반환한다.
	 * <p>
	 * 동기 호출에서는 polling을 하지 않으므로 이 값은 태스크 수행에 사용되지 않는다.
	 *
	 * @return	polling 주기. 옵션이 없으면 {@link FOption#empty()}.
	 * @throws IllegalArgumentException	옵션 값을 duration으로 해석할 수 없는 경우.
	 */
	public FOption<Duration> getPollInterval() {
		return m_descriptor.findOptionValue(OPTION_POLL_INTERVAL)
							.map(UnitUtils::parseSecondDuration);
	}
	
	/**
	 * 기술자의 {@value #OPTION_TIMEOUT} 옵션 값을 초 단위 duration으로 해석해 반환한다.
	 *
	 * @return	제한 시간. 옵션이 없으면 {@link FOption#empty()}.
	 * @throws IllegalArgumentException	옵션 값을 duration으로 해석할 수 없는 경우.
	 */
	public FOption<Duration> getTimeout() {
		return m_descriptor.findOptionValue(OPTION_TIMEOUT)
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

		return RESTfulRpcClient.builder()
								.setHttpClient(httpClient)
								.setOperationUrl(url)
								.setJsonMapper(MDTModelSerDe.getJsonMapper())
								.setInputs(args)
								.build();
	}
}
