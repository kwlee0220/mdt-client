package mdt.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.Data;

import org.jetbrains.annotations.Nullable;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import utils.InternalException;
import utils.LoggerSettable;
import utils.Tuple;
import utils.http.HttpClientProxy;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;
import utils.func.Nullables;

import mdt.client.operation.HttpSimulationClient;
import mdt.model.MDTExceptionEntity;


/**
 * {@link utils.http.HttpRESTfulClient} 도입 이전에 사용하던 OkHttp 기반 REST 클라이언트.
 * <p>
 * 현재는 {@link HttpSimulationClient}에서만 사용된다. 요청은 모두 동기적으로 전송되며,
 * 성공 응답의 본문은 JSON 매퍼로 역직렬화된다.
 * 오류 응답은 {@link MDTExceptionEntity}로 해석한 뒤 {@code code}에 담긴 클래스 이름의 예외를
 * 리플렉션으로 생성하여 던지고, 생성할 수 없으면 {@link RESTfulRemoteException}을 던진다.
 * 오류 응답 본문을 해석할 수 없으면 {@link InternalException}을 던진다.
 * 네트워크 오류는 {@link RESTfulIOException}으로 변환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpRESTfulClientOld implements HttpClientProxy, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpRESTfulClientOld.class);
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	private final String m_endpoint;
	/** 요청/응답 본문의 JSON 변환에 사용하는 매퍼. */
	protected final JsonMapper m_mapper;
	private Logger m_logger = null;
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	접속 대상 서버의 endpoint.
	 * @param jsonMapper	JSON 변환에 사용할 매퍼. {@code null}이면 기본 설정의 {@link JsonMapper}를 생성하여 사용한다.
	 */
	public HttpRESTfulClientOld(OkHttpClient client, String endpoint, @Nullable JsonMapper jsonMapper) {
		m_client = client;
		m_endpoint = endpoint;
		m_mapper = (jsonMapper != null) ? jsonMapper : JsonMapper.builder().build();
	}
	
	/**
	 * 기본 설정의 JSON 매퍼를 사용하는 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	접속 대상 서버의 endpoint.
	 */
	public HttpRESTfulClientOld(OkHttpClient client, String endpoint) {
		this(client, endpoint, null);
	}
	
	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	@Override
	public OkHttpClient getHttpClient() {
		return m_client;
	}
	
	/**
	 * JSON 변환에 사용하는 매퍼를 반환한다.
	 *
	 * @return	JSON 매퍼.
	 */
	public JsonMapper getJsonMapper() {
		return m_mapper;
	}
	
	/**
	 * 주어진 JSON 문자열로 요청 본문을 생성한다.
	 *
	 * @param jsonString	JSON 문자열.
	 * @return	JSON 요청 본문.
	 */
	public RequestBody createRequestBody(String jsonString) {
		return RequestBody.create(jsonString, JSON);
	}
	
	/**
	 * 주어진 객체를 JSON 요청 본문으로 변환한다. 문자열이면 그대로 사용한다.
	 *
	 * @param desc	요청 본문으로 사용할 객체.
	 * @return	JSON 요청 본문.
	 * @throws RESTfulIOException	JSON 직렬화에 실패한 경우.
	 */
	public RequestBody createRequestBody(Object desc) {
		try {
			String bodyStr = (desc instanceof String s) ? s : m_mapper.writeValueAsString(desc);
			return RequestBody.create(bodyStr, JSON);
		}
		catch ( JsonProcessingException e ) {
			String msg = String.format("Failed to write Json: object=%s, cause=%s", desc, e);
			throw new RESTfulIOException(msg);
		}
	}

	/**
	 * 요청을 동기적으로 전송하고 응답 본문을 주어진 타입으로 변환하여 반환한다.
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param cls	결과 타입 클래스.
	 * @return	변환된 결과.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 * @see #parseResponse(Response, Class)
	 */
	public <T> T call(Request req, Class<T> cls) throws RESTfulRemoteException {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, cls);
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("" + e);
		}
	}

	/**
	 * 요청을 동기적으로 전송하고 응답 본문을 주어진 타입으로 변환하여 반환한다.
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param typeRef	결과 타입 정보.
	 * @return	변환된 결과.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 */
	public <T> T call(Request req, TypeReference<T> typeRef) throws RESTfulRemoteException {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, typeRef);
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("" + e);
		}
	}

	/**
	 * 요청을 동기적으로 전송하고 응답 헤더와 변환된 응답 본문을 함께 반환한다.
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param typeRef	결과 타입 정보.
	 * @return	(응답 헤더, 변환된 결과) 쌍.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 */
	public <T> Tuple<Headers,T> callAndGetHeaders(Request req, TypeReference<T> typeRef) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return Tuple.of(resp.headers(), parseResponse(resp, typeRef));
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("" + e);
		}
	}

	/**
	 * 요청을 동기적으로 전송한다. 응답 본문은 무시하고 오류 응답인 경우에만 예외를 발생시킨다.
	 *
	 * @param req	전송할 요청.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속에 실패한 경우.
	 */
	public void send(Request req) throws RESTfulRemoteException {
		try {
			Response resp =  m_client.newCall(req).execute();
			if ( !resp.isSuccessful() ) {
				throwErrorResponse(resp.body().string());
			}
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("" + e);
		}
	}
	
	/**
	 * 응답 본문을 주어진 타입으로 변환한다.
	 * <p>
	 * 성공 응답인 경우 {@code cls}가 {@code String.class}이면 본문 문자열을,
	 * {@code void.class}이면 {@code null}을, 그 외에는 본문 JSON을 역직렬화한 객체를 반환한다.
	 *
	 * @param <T>	결과 타입.
	 * @param resp	HTTP 응답.
	 * @param cls	결과 타입 클래스.
	 * @return	변환된 결과.
	 * @throws RESTfulIOException	응답 본문을 읽거나 역직렬화하는 데 실패한 경우.
	 * @throws RESTfulRemoteException	오류 응답인 경우.
	 */
	@SuppressWarnings("unchecked")
	public <T> T parseResponse(Response resp, Class<T> cls) throws RESTfulIOException {
		try {
			String respBody = resp.body().string();
			if ( s_logger.isDebugEnabled() ) {
				Headers headers = resp.headers();
				s_logger.debug("received response: code={}, headers={}, body={}",
								resp.code(), headers, respBody);
			}
			
			if ( resp.isSuccessful() ) {
				if ( cls == String.class ) {
					return (T)respBody;
				}
				else if ( cls == void.class ) {
					return null;
				}
				else {
					return m_mapper.readValue(respBody, cls);
				}
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
//		catch ( JsonMappingException e ) {
//			
//		}
		catch ( IOException e ) {
			throw new RESTfulIOException(resp.toString() + ", cause=" + e);
		}
	}
	
	/**
	 * 응답 본문 JSON을 주어진 타입으로 역직렬화한다.
	 *
	 * @param <T>	결과 타입.
	 * @param resp	HTTP 응답.
	 * @param typeRef	결과 타입 정보.
	 * @return	변환된 결과.
	 * @throws RESTfulIOException	응답 본문을 읽거나 역직렬화하는 데 실패한 경우.
	 * @throws RESTfulRemoteException	오류 응답인 경우.
	 */
	public <T> T parseResponse(Response resp, TypeReference<T> typeRef) throws RESTfulIOException {
		try {
			String respBody = resp.body().string();
			if ( s_logger.isDebugEnabled() ) {
				Headers headers = resp.headers();
				s_logger.debug("received response: code={}, headers={}, body={}",
								resp.code(), headers, respBody);
			}
			
			if ( resp.isSuccessful() ) {
				return m_mapper.readValue(respBody, typeRef);
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException e ) {
			throw new RESTfulIOException(resp.toString() + ", cause=" + e);
		}
	}

	@Override
	public Logger getLogger() {
		return Nullables.orElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}

	private static final TypeReference<MDTExceptionEntity> EXCEPTION_ENTITY
																= new TypeReference<MDTExceptionEntity>(){};
//	private void throwErrorResponse(String respBody) throws MDTClientException {
//		try {
//			MDTExceptionEntity entity = parseJson(respBody, EXCEPTION_ENTITY);
//			throw entity.toClientException();
//		}
//		catch ( MDTClientException e ) {
//			throw e;
//		}
//		catch ( Exception e ) {
//			throw new InternalException("failed to parse ExceptionMessage: " + respBody);
//		}
//	}
	
	@SuppressWarnings("unchecked")
	private void throwErrorResponse(String respBody) throws RESTfulRemoteException {
		// '{"errors": [...]}' 형식이면 그 첫 오류로 예외를 만든다. 아니면 MDTExceptionEntity 형식으로 해석한다.
		String errorString = null;
		try {
			errorString = parseErrorString(respBody);
		}
		catch ( Exception notErrorResponseFormat ) { }
		if ( errorString != null ) {
			throw new RESTfulRemoteException(errorString);
		}
		
		MDTExceptionEntity entity = null;
		try {
			entity = m_mapper.readValue(respBody, EXCEPTION_ENTITY); 
		}
		catch ( Exception e ) {
			throw new InternalException("failed to parse ExceptionMessage: " + respBody);
		}

		Class<? extends RuntimeException> cls = null;
		try {
			cls = (Class<? extends RuntimeException>) Class.forName(entity.getCode());
		}
		catch ( Exception e ) {
			throw entity.toClientException();
		}
		
		if ( entity.getText() != null && entity.getText().length() > 0 ) {
			throw createException(entity, cls, entity.getText());
		}
		else {
			throw createException(entity, cls);
		}
	}
	
	private RuntimeException createException(MDTExceptionEntity entity,
											Class<? extends RuntimeException> entityCls, String details) {
		try {
			Constructor<? extends RuntimeException> ctor = entityCls.getConstructor(String.class);
			return ctor.newInstance(details);
		}
		catch ( Exception e ) {
			return entity.toClientException();
		}
	}
	
	private RuntimeException createException(MDTExceptionEntity entity,
												Class<? extends RuntimeException> entityCls) {
		try {
			Constructor<? extends RuntimeException> ctor = entityCls.getConstructor();
			return ctor.newInstance();
		}
		catch ( Exception e ) {
			return entity.toClientException();
		}
	}
	
	/**
	 * {@code {"errors": [{"code": ..., "message": ...}]}} 형식의 오류 응답에서 첫 오류를 문자열로 변환한다.
	 *
	 * @param respBody	오류 응답 본문.
	 * @return	{@code code=<code>, message=<message>} 형식의 문자열.
	 * @throws JsonMappingException	응답 본문을 {@link ErrorResponse}로 변환할 수 없는 경우.
	 * @throws JsonProcessingException	응답 본문이 올바른 JSON이 아닌 경우.
	 */
	protected String parseErrorString(String respBody) throws JsonMappingException, JsonProcessingException {
		ErrorResponse resp = m_mapper.readValue(respBody, ErrorResponse.class);
		ErrorMessage msg = resp.errors.get(0);
		return String.format("code=%s, message=%s", msg.code, msg.message);
	}

	/**
	 * {@code errors} 배열을 가진 오류 응답에 대응하는 JSON 매핑 클래스.
	 */
	@Data
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonInclude(Include.NON_NULL)
	public static class ErrorResponse {
		private List<ErrorMessage> errors;
	}
	
	@SuppressWarnings("unused")
	private static final TypeReference<List<ErrorMessage>> ERROR_LIST_TYPE
														= new TypeReference<List<ErrorMessage>>(){};
	/**
	 * 오류 응답의 개별 오류({@code code}, {@code message})에 대응하는 JSON 매핑 클래스.
	 */
	@Data
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonInclude(Include.NON_NULL)
	public static class ErrorMessage {
		private String code;
		private String message;
	}
}
