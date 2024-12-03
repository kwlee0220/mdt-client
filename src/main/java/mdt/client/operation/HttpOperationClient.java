package mdt.client.operation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.model.AASUtils;
import mdt.model.MDTModelSerDe;
import mdt.task.Parameter;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpOperationClient extends AbstractThreadedExecution<List<Parameter>>
									implements HttpClientProxy, CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpOperationClient.class);
	private static final JsonMapper MAPPER = MDTModelSerDe.getJsonMapper();
	
	private final OkHttpClient m_httpClient;
	private final String m_endpoint;
	private final String m_startUrl;
	private final OperationRequestBody m_request;
	private final Duration m_pollInterval;
	private final Duration m_timeout;
	
	private final HttpRESTfulClient m_restfulStatusClient;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private Thread m_workerThread = null;
	
	private HttpOperationClient(Builder builder) {
		Preconditions.checkNotNull(builder.m_httpClient);
		Preconditions.checkNotNull(builder.m_endpoint);
		Preconditions.checkNotNull(builder.m_startUrl);
		
		m_httpClient = builder.m_httpClient;
		m_endpoint = builder.m_endpoint;
		m_startUrl = builder.m_startUrl;
		m_request = builder.m_requestBody;
		m_pollInterval = builder.m_pollInterval;
		m_timeout = builder.m_timeout;
		
		m_restfulStatusClient = HttpRESTfulClient.builder()
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
	protected List<Parameter> executeWork() throws InterruptedException, CancellationException,
																			Exception {
		m_guard.run(() -> m_workerThread = Thread.currentThread());
		
		Instant started = Instant.now();
		OperationResponse resp = start(m_request);
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("received from HTTPOperationServer: {}", resp);
		}
		
		String encodedSessionId = AASUtils.encodeBase64UrlSafe(resp.getSession());
		
		boolean isFirst = true;
		String statusUrl = String.format("%s/sessions/%s", m_endpoint, encodedSessionId);
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			if ( isCancelRequested() ) {
				resp = cancel();
			}
			else {
				try {
					if ( !isFirst ) {
						TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
					}
					else {
						isFirst = false;
					}
					
					resp = m_restfulStatusClient.get(statusUrl, m_opRespDeser);
					if ( getLogger().isInfoEnabled() ) {
						getLogger().info("received from HTTPOperationServer: {}", resp);
					}
				}
				catch ( InterruptedException e ) {
					resp = cancel();
				}
			}
			
			// timeout이 설정된 경우에는 소요시간을 체크하여 제한시간을 경과한 경우에는
			// TimeoutException 예외를 발생시킨다.
			if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					resp = cancel();
					if ( resp.getStatus() == OperationStatus.CANCELLED ) {
						String msg = String.format("timeout=%s", m_timeout);
						throw new TimeoutException(msg);
					}
				}
			}
		}
		m_guard.run(() -> m_workerThread = null);
		
		String msg;
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return resp.getOutputValues();
			case FAILED:
				throw (Exception)resp.toJavaException();
			case CANCELLED:
				msg = String.format("cause=%s%n", resp.getMessage());
				throw new CancellationException(msg);
			default:
				throw new AssertionError();
		}
	}

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
	
	private OperationResponse start(OperationRequestBody request) {
		HttpRESTfulClient client = HttpRESTfulClient.builder()
												.httpClient(m_httpClient)
												.errorEntityDeserializer(new JacksonErrorEntityDeserializer(MAPPER))
												.jsonMapper(MAPPER)
												.build();
		
		String requestJson = MDTModelSerDe.toJsonString(request);
		RequestBody reqBody = RequestBody.create(requestJson, HttpRESTfulClient.MEDIA_TYPE_JSON);
		return client.post(m_startUrl, reqBody, m_opRespDeser);
	}
	
	private OperationResponse cancel() {
		String url = getEndpoint();
		return m_restfulStatusClient.delete(url, m_opRespDeser);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static final class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private String m_startUrl;
		private OperationRequestBody m_requestBody;
		private Duration m_pollInterval = Duration.ofSeconds(3);
		private Duration m_timeout;
		
		public HttpOperationClient build() {
			
			return new HttpOperationClient(this);
		}
		
		public Builder setHttpClient(OkHttpClient http) {
			m_httpClient = http;
			return this;
		}
		
		public Builder setEndpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
		
		public Builder setStartUrl(String url) {
			m_startUrl = url;
			return this;
		}
		
		public Builder setRequestBody(OperationRequestBody body) {
			m_requestBody = body;
			return this;
		}
		
		public Builder setPollInterval(Duration interval) {
			m_pollInterval = interval;
			return this;
		}
		
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
			return MAPPER.readValue(respBody, RESPONSE_TYPE_REF);
		}
	};
}
