package mdt.client.operation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.Preconditions;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.thread.Guard;

import mdt.client.MDTRESTfulClients;
import mdt.model.MDTModelSerDe;


/**
 * HTTP 연산 서버에 연산 실행을 요청하고, 완료될 때까지 세션 상태를 polling 하는 비동기 실행.
 * <p>
 * 실행이 시작되면 {@code <endpoint>/operations}에 {@link OperationRequest}를 POST 하여 세션을 생성하고,
 * 응답 상태가 {@link OperationStatus#RUNNING}인 동안 {@code <endpoint>/sessions/<sessionId>}를
 * 주기적으로 GET 하여 상태를 확인한다. 최종 상태에 따라 다음과 같이 종료된다.
 * <ul>
 *   <li>{@link OperationStatus#COMPLETED}: 마지막 {@link OperationResponse}를 결과로 반환한다.</li>
 *   <li>{@link OperationStatus#FAILED}: {@link OperationResponse#toJavaException()}으로 복원한 예외를 던진다.</li>
 *   <li>{@link OperationStatus#CANCELLED}: {@link CancellationException}을 던진다.</li>
 * </ul>
 * 취소가 요청되거나 polling 대기 중 인터럽트되면 세션에 DELETE 요청을 보내 연산 취소를 요청한다.
 * 제한 시간이 설정된 경우, 제한 시간이 경과하면 취소를 요청하고 취소된 경우 {@link TimeoutException}을 던진다.
 *
 * @author Kang-Woo Lee (ETRI)
 * @deprecated	{@code mdt-operation-http}의 {@code OperationController}와 URL 및 메시지 형식이 맞지 않아
 * 			사용할 수 없다. 대신 {@link utils.rpc.restful.RESTfulAsyncRpcClient}를 사용한다.
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public class HttpOperationClient extends AbstractThreadedExecution<OperationResponse>
									implements HttpClientProxy, CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpOperationClient.class);
	private static final JsonMapper MAPPER = MDTModelSerDe.getJsonMapper();
	
	private final OkHttpClient m_httpClient;
	private final String m_endpoint;
	private final OperationRequest m_request;
	private final Duration m_pollInterval;
	private final Duration m_timeout;
	
	private final HttpRESTfulClient m_restClient;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private Thread m_workerThread = null;
	
	private HttpOperationClient(Builder builder) {
		Preconditions.checkNotNullArgument(builder.m_httpClient, "'httpClient' is null");
		Preconditions.checkNotNullArgument(builder.m_endpoint, "'endpoint' is null");
		
		m_httpClient = builder.m_httpClient;
		m_endpoint = builder.m_endpoint;
		m_request = builder.m_requestBody;
		m_pollInterval = builder.m_pollInterval;
		m_timeout = builder.m_timeout;
		
		m_restClient = MDTRESTfulClients.builder()
										.httpClient(builder.m_httpClient)
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(MAPPER))
										.jsonMapper(MAPPER)
										.build();
		
		setLogger(s_logger);
	}
	
	@Override
	public OkHttpClient getHttpClient() {
		return m_httpClient;
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	protected void initializeThread() throws Exception {
		m_guard.run(() -> m_workerThread = Thread.currentThread());
	}

	@Override
	protected OperationResponse executeWork() throws InterruptedException, CancellationException,
																			Exception {
		try {
			Instant started = Instant.now();
			OperationResponse resp = start(m_request);
			getLogger().info("received from HTTPOperationServer: {}", resp);

			String sessionId = resp.getSessionId();

			boolean isFirst = true;
			String statusUrl = String.format("%s/sessions/%s", m_endpoint, sessionId);
			while ( resp.getStatus() == OperationStatus.RUNNING ) {
				if ( isCancelRequested() ) {
					resp = sendCancelRequest(sessionId);
					break;
				}
				else {
					try {
						if ( !isFirst ) {
							TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
						}
						else {
							isFirst = false;
						}

						resp = m_restClient.get(statusUrl, m_opRespDeser);
						getLogger().info("received from HTTPOperationServer: {}", resp);
					}
					catch ( InterruptedException e ) {
						resp = sendCancelRequest(sessionId);
					}
				}

				// timeout이 설정된 경우에는 소요시간을 체크하여 제한시간을 경과한 경우에는
				// TimeoutException 예외를 발생시킨다.
				if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
					if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
						resp = sendCancelRequest(sessionId);
						if ( resp.getStatus() == OperationStatus.CANCELLED ) {
							String msg = String.format("timeout=%s", m_timeout);
							throw new TimeoutException(msg);
						}
					}
				}
			}

			String msg;
			switch ( resp.getStatus() ) {
				case COMPLETED:
					return resp;
				case FAILED:
					throw (Exception)resp.toJavaException();
				case CANCELLED:
					msg = String.format("cause=%s%n", resp.getMessage());
					throw new CancellationException(msg);
				default:
					throw new AssertionError();
			}
		}
		finally {
			// 정상/예외 종료에 관계없이 worker thread 참조를 정리하여,
			// 이후 cancelWork()가 재사용된 다른 thread를 잘못 interrupt하지 않도록 한다.
			m_guard.run(() -> m_workerThread = null);
		}
	}

	/**
	 * 상태 polling 중인 작업 쓰레드를 인터럽트하여 원격 연산 취소를 유도한다.
	 * <p>
	 * 인터럽트된 작업 쓰레드는 세션에 취소 요청을 보낸다. 작업 쓰레드가 없더라도 항상 {@code true}를 반환한다.
	 *
	 * @return	항상 {@code true}.
	 */
	@Override
	public boolean cancelWork() {
		// status check thread가 sleep하고 있을 수 있기 때문에
		// 해당 thread를 interrupt시킨다.
		return m_guard.get(() -> {
			if ( m_workerThread != null ) {
				m_workerThread.interrupt();
			}
			return true;
		});
	}
	
	private OperationResponse start(OperationRequest request) {
		Preconditions.checkState(request.getOperation() != null, "'operation' is null");
		
		String requestJson = MDTModelSerDe.toJsonString(request);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(requestJson); 
		return m_restClient.post(m_endpoint + "/operations", reqBody, m_opRespDeser);
	}
	
	private OperationResponse sendCancelRequest(String sessionId) {
		String deleteUrl = String.format("%s/sessions/%s", m_endpoint, sessionId);
		return m_restClient.delete(deleteUrl, m_opRespDeser);
	}
	
	/**
	 * {@link HttpOperationClient} 객체를 생성하기 위한 빌더를 반환한다.
	 *
	 * @return	새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link HttpOperationClient} 객체 생성을 위한 빌더.
	 * <p>
	 * HTTP 클라이언트와 endpoint는 반드시 설정되어야 한다.
	 */
	public static final class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private OperationRequest m_requestBody;
		private Duration m_pollInterval = Duration.ofSeconds(3);
		private Duration m_timeout;
		
		/**
		 * 설정된 값으로 {@link HttpOperationClient} 객체를 생성한다.
		 *
		 * @return	생성된 {@link HttpOperationClient} 객체.
		 * @throws IllegalArgumentException	HTTP 클라이언트 또는 endpoint가 설정되지 않은 경우.
		 */
		public HttpOperationClient build() {
			return new HttpOperationClient(this);
		}
		
		/**
		 * HTTP 연산 서버 접근에 사용할 HTTP 클라이언트를 설정한다.
		 *
		 * @param http	HTTP 클라이언트.
		 * @return	빌더 자신.
		 */
		public Builder setHttpClient(OkHttpClient http) {
			m_httpClient = http;
			return this;
		}
		
		/**
		 * HTTP 연산 서버의 endpoint URL을 설정한다.
		 *
		 * @param endpoint	연산 서버 endpoint URL.
		 * @return	빌더 자신.
		 */
		public Builder setEndpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
		
		/**
		 * 연산 서버에 전송할 연산 요청을 설정한다.
		 * <p>
		 * 요청의 {@code operation}은 실행 시점에 {@code null}이 아니어야 한다.
		 *
		 * @param body	연산 요청.
		 * @return	빌더 자신.
		 */
		public Builder setRequestBody(OperationRequest body) {
			m_requestBody = body;
			return this;
		}
		
		/**
		 * 세션 상태 확인 주기를 설정한다. 기본값은 3초이다.
		 *
		 * @param interval	상태 확인 주기.
		 * @return	빌더 자신.
		 */
		public Builder setPollInterval(Duration interval) {
			m_pollInterval = interval;
			return this;
		}
		
		/**
		 * 연산 수행 제한 시간을 설정한다. 기본값은 {@code null}(무제한)이다.
		 *
		 * @param timeout	제한 시간. {@code null}이면 무제한.
		 * @return	빌더 자신.
		 */
		public Builder setTimeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
	}

	private static final TypeReference<OperationResponse> RESPONSE_TYPE_REF
													= new TypeReference<OperationResponse>(){};
	private ResponseBodyDeserializer<OperationResponse> m_opRespDeser = new ResponseBodyDeserializer<>() {
		@Override
		public OperationResponse deserialize(Headers headers, String respBody) throws IOException {
			return OperationResponse.fromJsonString(respBody);
//			return MAPPER.readValue(respBody, RESPONSE_TYPE_REF);
		}
	};
}
