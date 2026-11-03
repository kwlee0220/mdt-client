package mdt.client.registry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import utils.http.HttpRESTfulClient;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.client.Fa3stMessage;

/**
 * AAS4J JSON 직렬화기를 이용하여 FA³ST 레지스트리 서버를 호출하는 HTTP 클라이언트의 공통 기반 클래스.
 * <p>
 * 오류 응답은 {@link Fa3stMessage}로 해석하여 {@code code}에 담긴 클래스 이름의 예외를 생성하여 던지고,
 * 생성할 수 없으면 {@link RESTfulRemoteException}을 던진다.
 * 현재는 이 클래스를 상속하는 클래스가 없다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
class HttpRegistryClient {
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	private final JsonSerializer m_ser;
	private final JsonDeserializer m_deser;
	
	public HttpRegistryClient(OkHttpClient client) {
		m_client = client;
		m_ser = new JsonSerializer();
		m_deser = new JsonDeserializer();
	}
	
	public OkHttpClient getHttpClient() {
		return m_client;
	}
	
	protected RequestBody createRequestBody(Object desc) throws SerializationException {
		String reqBodyStr = m_ser.write(desc);
		return HttpRESTfulClient.newJsonRequestBody(reqBodyStr);
	}

	protected <T> T call(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, resultCls);
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulRemoteException("" + e);
		}
	}
	
	protected <T> List<T> callList(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponseToList(resp, resultCls);
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + req.url(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulRemoteException("" + e);
		}
	}

	protected void send(Request req) {
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
			throw new RESTfulRemoteException("" + e);
		}
	}
	
	private <T> T parseResponse(Response resp, Class<T> valueType)
		throws RESTfulRemoteException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				return m_deser.read(respBody, valueType);
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException | DeserializationException e ) {
			throw new RESTfulRemoteException(resp.toString());
		}
	}
	
	private <T> List<T> parseResponseToList(Response resp, Class<T> valueType)
		throws RESTfulRemoteException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				return m_deser.readList(respBody, valueType);
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException | DeserializationException e ) {
			throw new RESTfulRemoteException(resp.toString());
		}
	}
	
	private void throwErrorResponse(String respBody) throws RESTfulRemoteException {
		Fa3stMessage msg = null;
		try {
			msg = m_deser.read(respBody, Fa3stMessage.class);
			@SuppressWarnings("unchecked")
			Class<? extends Throwable> cls = (Class<? extends Throwable>) Class.forName(msg.getCode());
			Constructor<? extends Throwable> ctor = cls.getConstructor(String.class);
			throw (RuntimeException)ctor.newInstance(msg.getText());
		}
		catch ( RESTfulRemoteException e ) { throw e; }
		catch ( Exception e ) {
			String details = ( msg != null )
							? msg.getCode() + ": " + msg.getText() + ", ts=" + msg.getTimestamp()
							: respBody;
			throw new RESTfulRemoteException(details);
		}
	}
}
