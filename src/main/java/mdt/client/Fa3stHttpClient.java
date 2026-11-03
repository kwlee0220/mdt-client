package mdt.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import utils.Tuple;
import utils.http.HttpClientProxy;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;


/**
 * FA³ST 서버의 HTTP(REST) 인터페이스를 호출하는 클라이언트들의 공통 기반 클래스.
 * <p>
 * OkHttp를 이용하여 요청을 동기적으로 전송하고, 응답 본문을 {@link MDTModelSerDe}로 역직렬화한다.
 * 응답 JSON에 {@code result} 필드가 있으면 그 내용(목록이면 첫 원소)을 결과로 사용한다.
 * 오류 응답은 FA³ST의 {@code messages} 형식({@link Fa3stMessage})으로 해석하여 예외로 변환한다.
 * <ul>
 * 	<li>{@code code}가 비어 있고 HTTP 상태가 404이면 {@link ResourceNotFoundException}.
 * 	<li>{@code code}가 비어 있고 HTTP 상태가 5xx이면 {@link RESTfulRemoteException}.
 * 	<li>{@code code}가 {@code null}이면 {@link RESTfulRemoteException}.
 * 	<li>그 외에는 {@code code}를 클래스 이름으로 간주하여 해당 예외 객체를 생성하여 던진다.
 * 		생성에 실패하면 {@link RESTfulIOException}을 던진다.
 * </ul>
 * 네트워크 오류는 모두 {@link RESTfulIOException}으로 변환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Fa3stHttpClient implements HttpClientProxy {
	private static final Logger s_logger = LoggerFactory.getLogger(Fa3stHttpClient.class);
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final String m_endpoint;
	private final OkHttpClient m_client;
//	private Supplier<AASFile> m_aasFileFactory = () -> MemoryAASFile.builder().build();
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	접속 대상 서버의 endpoint.
	 */
	public Fa3stHttpClient(OkHttpClient client, String endpoint) {
		m_endpoint = endpoint;
		m_client = client;
	}
	
	/**
	 * 요청 전송에 사용하는 OkHttp 클라이언트를 반환한다.
	 *
	 * @return	OkHttp 클라이언트.
	 */
	public OkHttpClient getHttpClient() {
		return m_client;
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	/**
	 * 주어진 객체를 JSON 요청 본문으로 변환한다.
	 * <p>
	 * {@code desc}가 문자열이면 그대로 사용하고, 그렇지 않으면 {@link MDTModelSerDe}로
	 * JSON 직렬화한다.
	 *
	 * @param desc	요청 본문으로 사용할 객체.
	 * @return	JSON 요청 본문.
	 * @throws IOException	JSON 직렬화에 실패한 경우.
	 */
	protected RequestBody createRequestBody(Object desc) throws IOException {
		if ( desc instanceof String str ) {
			return RequestBody.create(str, JSON);
		}
		else {
			String jsonStr = MDTModelSerDe.toJsonString(desc);
			return RequestBody.create(jsonStr, JSON);
		}
	}

	/**
	 * 요청을 동기적으로 전송하고 응답 본문을 주어진 타입으로 변환하여 반환한다.
	 * <p>
	 * {@code call(req, resultCls, null)}과 동일하다.
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param resultCls	결과 타입 클래스.
	 * @return	변환된 결과. 응답 상태가 204이거나 본문이 비어 있으면 {@code null}.
	 * @see #call(Request, Class, Object)
	 */
	protected <T> T call(Request req, Class<T> resultCls) {
		return call(req, resultCls, null);
	}

	/**
	 * 요청을 동기적으로 전송하고 응답 본문을 주어진 타입으로 변환하여 반환한다.
	 * <p>
	 * {@code resultCls}에 따라 응답 본문을 다음과 같이 처리한다.
	 * <ul>
	 * 	<li>{@code void.class}: 응답 메시지의 첫 text가 {@code "OK"}인지 확인하고 {@code null}을 반환한다.
	 * 	<li>{@code byte[].class}: 본문 바이트 배열을 반환한다.
	 * 	<li>{@link OutputStream} 타입: 본문을 {@code attachment} 스트림에 복사하고
	 * 		두 스트림을 닫은 후 {@code null}을 반환한다.
	 * 	<li>{@link JsonNode} 타입: 본문을 JSON 트리로 반환한다.
	 * 	<li>그 외: 본문 JSON을 역직렬화한다. {@code result} 필드가 있으면 그 첫 원소를 사용한다.
	 * </ul>
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param resultCls	결과 타입 클래스.
	 * @param attachment	{@code resultCls}가 {@link OutputStream} 타입인 경우 응답 본문을 기록할
	 * 					출력 스트림. 그 외의 경우는 사용되지 않는다.
	 * @return	변환된 결과. 응답 상태가 204이거나 본문이 비어 있으면 {@code null}.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	protected <T> T call(Request req, Class<T> resultCls, Object attachment) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, resultCls, attachment);
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + m_endpoint, e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("" + e);
		}
	}

	/**
	 * 비동기 연산 시작 요청을 전송하고 응답의 {@code Location} 헤더와 결과를 함께 반환한다.
	 * <p>
	 * 요청 자체는 동기적으로 전송된다. 응답 상태가 202(Accepted)인 경우에만
	 * {@code Location} 헤더 값을 반환하고, 그 외에는 {@code null}을 반환한다.
	 *
	 * @param <T>	결과 타입.
	 * @param req	전송할 요청.
	 * @param resultCls	결과 타입 클래스.
	 * @return	({@code Location} 헤더 값, 변환된 결과) 쌍.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	protected <T> Tuple<String,T> callAsync(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			T result = parseResponse(resp, resultCls, null);
			if ( resp.code() == 202 ) {
				return Tuple.of(resp.header("Location"), result);
			}
			else {
				return Tuple.of(null, result);
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
	 * 요청을 동기적으로 전송하고 응답의 {@code result} 배열을 주어진 타입의 리스트로 변환하여 반환한다.
	 *
	 * @param <T>	리스트 원소 타입.
	 * @param req	전송할 요청.
	 * @param resultCls	리스트 원소 타입 클래스.
	 * @return	결과 리스트.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	protected <T> List<T> callList(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponseToList(resp, resultCls);
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
	 * @throws RESTfulIOException	서버 접속에 실패한 경우.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	protected void send(Request req) {
		try {
			Response resp =  m_client.newCall(req).execute();
			if ( !resp.isSuccessful() ) {
				throwErrorResponse(resp, resp.body().string());
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
	private <T> T parseResponse(Response resp, Class<T> valueType, Object attachment) throws RESTfulIOException {
		try {
			if ( resp.isSuccessful() ) {
				if ( resp.code() != 204 ) {
					try ( ResponseBody respBody = resp.body() ) {
						if ( void.class == valueType ) {
							Messages msgs = MDTModelSerDe.readValue(respBody.string(), Messages.class);
							Fa3stMessage msg = msgs.m_messages.get(0);
							if ( !msg.getText().equals("OK") ) {
								throw new RESTfulRemoteException("No exception raised but not OK");
							}
							return null;
						}
						else if ( byte[].class == valueType ) {
							return (T)respBody.bytes();
						}
						else if ( OutputStream.class.isAssignableFrom(valueType) ) {
							Preconditions.checkArgument(attachment != null && attachment instanceof OutputStream,
														"invalid response attachment for File response");
							
							InputStream in = respBody.byteStream();
							OutputStream out = (OutputStream)attachment;
							try ( in; out  ) {
								long nbytes = in.transferTo(out);
								s_logger.info("downloaded: {} bytes", nbytes);
							}
							
							return null;
						}
						else if ( JsonNode.class.isAssignableFrom(valueType) ) {
							return (T)MDTModelSerDe.MAPPER.readTree(respBody.string());
						}
						else {
							String respBodyStr = respBody.string();
							if ( respBodyStr.length() > 0 ) {
								JsonNode root = MDTModelSerDe.MAPPER.readTree(respBodyStr);
								
								JsonNode results = root.get("result");
								if ( results != null ) {
									return MDTModelSerDe.readValue(results.get(0), valueType);
								}
								else {
									return MDTModelSerDe.readValue(root, valueType);
								}
							}
							else {
								return null;
							}
						}
						
					}
				}
				else {
					return null;
				}
			}
			else {
				try ( ResponseBody respBody = resp.body() ) {
					String respBodyStr = respBody.string();
					throwErrorResponse(resp, respBodyStr);
					throw new AssertionError();
				}
			}
		}
		catch ( IOException e ) {
			throw new RESTfulIOException(e.toString());
		}
	}
	
	private <T> List<T> parseResponseToList(Response resp, Class<T> valueType)
		throws RESTfulIOException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				JsonMapper mapper = JsonMapper.builder().build();
				JsonNode root = mapper.readTree(respBody);
				JsonNode result = root.path("result");
				return MDTModelSerDe.readValueList(result, valueType);
			}
			else {
				throwErrorResponse(resp, respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException e ) {
			throw new RESTfulIOException(resp.toString());
		}
	}
	
	/**
	 * FA³ST 서버의 응답 메시지 목록({@code {"messages": [...]}})에 대응하는 JSON 매핑 클래스.
	 */
	public static final class Messages {
		@JsonProperty("messages") 
		private List<Fa3stMessage> m_messages;
	}
	
	private void throwErrorResponse(Response resp, String respBody) {
		Fa3stMessage msg = null;
		
		try {
			Messages msgs = MDTModelSerDe.getJsonMapper().readValue(respBody, Messages.class);
			msg = msgs.m_messages.get(0);
			if ( msg.getCode() == null || msg.getCode().length() == 0 ) {
				if ( resp.code() == 404 ) {
					throw new ResourceNotFoundException(msg.getText());
				}
				else if ( resp.code() >= 500 && resp.code() < 600 ) {
					throw msg.toClientException();
				}
			}
			if ( msg.getCode() == null ) {
				String details = msg.getText() + ", ts=" + msg.getTimestamp();
				throw new RESTfulRemoteException(details);
			}
			
			@SuppressWarnings("unchecked")
			Class<? extends Throwable> cls = (Class<? extends Throwable>) Class.forName(msg.getCode());
			Constructor<? extends Throwable> ctor = cls.getConstructor(String.class);
			throw (RuntimeException)ctor.newInstance(msg.getText());
		}
		catch ( IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException
				| IllegalAccessException | InstantiationException e ) {
			String details = ( msg != null )
							? msg.getCode() + ": " + msg.getText() + ", ts=" + msg.getTimestamp()
							: respBody;
			throw new RESTfulIOException(details);
		}
	}
}
