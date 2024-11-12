package mdt.client.registry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import utils.http.RESTfulRemoteException;

import mdt.client.Fa3stMessage;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
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
		return RequestBody.create(reqBodyStr, JSON);
	}

	protected <T> T call(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, resultCls);
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
