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

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import utils.InternalException;
import utils.LoggerSettable;
import utils.Tuple;
import utils.func.Optionals;
import utils.http.HttpClientProxy;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.client.operation.HttpSimulationClient;
import mdt.model.MDTExceptionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpRESTfulClientOld implements HttpClientProxy, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpSimulationClient.class);
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	private final String m_endpoint;
	protected final JsonMapper m_mapper;
	private Logger m_logger = null;
	
	public HttpRESTfulClientOld(OkHttpClient client, String endpoint, @Nullable JsonMapper jsonMapper) {
		m_client = client;
		m_endpoint = endpoint;
		m_mapper = (jsonMapper != null) ? jsonMapper : JsonMapper.builder().build();
	}
	
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
	
	public JsonMapper getJsonMapper() {
		return m_mapper;
	}
	
	public RequestBody createRequestBody(String jsonString) {
		return RequestBody.create(jsonString, JSON);
	}
	
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
		return Optionals.getOrElse(m_logger, s_logger);
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
		try {
			String msg = parseErrorString(respBody);
			throw new RESTfulRemoteException(msg);
		}
		catch ( Exception e ) { }
		
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
	
	protected String parseErrorString(String respBody) throws JsonMappingException, JsonProcessingException {
		ErrorResponse resp = m_mapper.readValue(respBody, ErrorResponse.class);
		ErrorMessage msg = resp.errors.get(0);
		return String.format("code=%s, message=%s", msg.code, msg.message);
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonInclude(Include.NON_NULL)
	public static class ErrorResponse {
		private List<ErrorMessage> errors;
	}
	
	@SuppressWarnings("unused")
	private static final TypeReference<List<ErrorMessage>> ERROR_LIST_TYPE
														= new TypeReference<List<ErrorMessage>>(){};
	@Data
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonInclude(Include.NON_NULL)
	public static class ErrorMessage {
		private String code;
		private String message;
	}
}
