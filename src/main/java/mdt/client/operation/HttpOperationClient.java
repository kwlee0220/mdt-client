package mdt.client.operation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.InternalException;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.func.KeyValue;
import utils.stream.FStream;

import mdt.client.HttpRESTfulClient;
import mdt.client.MDTClientException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpOperationClient extends AbstractThreadedExecution<JsonNode> implements CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpOperationClient.class);
	private static final TypeReference<OperationStatusResponse<JsonNode>> RESPONSE_TYPE_REF
													= new TypeReference<OperationStatusResponse<JsonNode>>(){};
	public static final JsonMapper MAPPER = new JsonMapper();
	
	private final HttpRESTfulClient m_restClient;
	private final String m_endpoint;
	private final String m_requestBodyJson;
	private final Duration m_pollInterval;
	private final Duration m_timeout;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private Thread m_workerThread = null;
	
	private HttpOperationClient(Builder builder) {
		m_restClient = builder.m_restClient;
		m_endpoint = builder.m_endpoint;
		m_requestBodyJson = builder.m_requestBodyJson;
		m_pollInterval = builder.m_pollInterval;
		m_timeout = builder.m_timeout;
	}

	@Override
	protected JsonNode executeWork() throws InterruptedException, CancellationException, TimeoutException,
											Exception {
		m_guard.run(() -> m_workerThread = Thread.currentThread());
		
		Instant started = Instant.now();
		OperationStatusResponse<JsonNode> resp = start(m_requestBodyJson);
		
		String location = resp.getOperationLocation();
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			if ( location == null ) {
				m_guard.run(() -> m_workerThread = null);
				throw new Exception("Protocol mismatch: 'Location' is missing");
			}
			if ( isCancelRequested() ) {
				resp = cancel(location);
			}
			else {
				try {
					TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
					resp = getStatus(location);
				}
				catch ( InterruptedException e ) {
					resp = cancel(location);
				}
			}
			
			// timeout이 설정된 경우에는 소요시간을 체크하여 제한시간을 경과한 경우에는
			// TimeoutException 예외를 발생시킨다.
			if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					resp = cancel(location);
					if ( resp.getStatus() == OperationStatus.CANCELLED ) {
						String msg = String.format("id=%s, timeout=%s", location, m_timeout);
						throw new TimeoutException(msg);
					}
				}
			}
		}
		m_guard.run(() -> m_workerThread = null);
		
		String msg;
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return resp.getResult();
			case FAILED:
				msg = String.format("OperationServer failed: id=%s, cause=%s%n", location, resp.getMessage());
				throw new MDTClientException(msg);
			case CANCELLED:
				msg = String.format("id=%s, cause=%s%n", location, resp.getMessage());
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
	
	private OperationStatusResponse<JsonNode> start(String requestBody) {
		RequestBody body = m_restClient.createRequestBody(requestBody);
		
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending start request: url={}, body={}", m_endpoint, requestBody);
		}
		Request req = new Request.Builder().url(m_endpoint).post(body).build();
		
		try {
			Response resp =  m_restClient.getHttpClient().newCall(req).execute();
			OperationStatusResponse<JsonNode> opStatus = m_restClient.parseResponse(resp, RESPONSE_TYPE_REF);
			opStatus.setOperationLocation(resp.header("Location"));
			return opStatus;
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	private OperationStatusResponse<JsonNode> getStatus(String opId) {
		opId = opId.trim();
		String url = (opId.length() > 0) ? String.format("%s/%s", m_endpoint, opId) : m_endpoint;

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (GET) {}", url);
		}
		Request req = new Request.Builder().url(url).get().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
	
	private OperationStatusResponse<JsonNode> cancel(String opId) {
		String url = String.format("%s/%s", m_endpoint, opId);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (DELETE) {}", url);
		}
		Request req = new Request.Builder().url(url).delete().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}

	public static String buildParametersJsonString(Map<String,Object> parameters)
		throws JsonProcessingException {
		return MAPPER.writeValueAsString(buildParametersJson(parameters));
	}
	public static ObjectNode buildParametersJson(Map<String,?> parameters) {
		return FStream.from(parameters)
						.mapValue(HttpOperationClient::toJsonNode)
						.fold(MAPPER.createObjectNode(), (on,kv) -> on.set(kv.key(), kv.value()));
	}
	private static JsonNode toJsonNode(Object obj) {
		String str = (obj instanceof String s) ? s : "" + obj;
		str = str.trim();
		if ( str.startsWith("{") ) {
			try {
				return MAPPER.readTree(str);
			}
			catch ( Exception e ) {
				throw new IllegalArgumentException("Failed to parse Json: " + str);
			}
		}
		else {
			return new TextNode(str);
		}
	}
	
	public static Map<String,String> parseParametersJson(String parametersJson) {
		try {
			return FStream.from(MAPPER.readTree(parametersJson).properties())
								.map(ent -> toParameter(ent.getKey(), ent.getValue()))
								.toMap(KeyValue::key, KeyValue::value);
		}
		catch ( Exception e ) {
			throw new IllegalArgumentException("invalid Json: " + parametersJson + ", cause=" + e);
		}
	}
	private static KeyValue<String,String> toParameter(String paramName, JsonNode paramValue) {
		if ( paramValue.isObject() ) {
			try {
				return KeyValue.of(paramName, MAPPER.writeValueAsString(paramValue));
			}
			catch ( JsonProcessingException e ) {
				throw new InternalException("" + e);
			}
		}
		else {
			return KeyValue.of(paramName, paramValue.asText());
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static final class Builder {
		private HttpRESTfulClient m_restClient;
		private String m_endpoint;
		private String m_requestBodyJson;
		private Duration m_pollInterval = Duration.ofSeconds(3);
		private Duration m_timeout;
		
		public HttpOperationClient build() {
			return new HttpOperationClient(this);
		}
		
		public Builder setHttpClient(OkHttpClient http) {
			m_restClient = new HttpRESTfulClient(http);
			return this;
		}
		
		public Builder setEndpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
		
		public Builder setRequestBodyJson(String json) {
			m_requestBodyJson = json;
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
}
