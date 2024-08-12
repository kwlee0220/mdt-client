package mdt.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import utils.InternalException;
import utils.func.Lazy;

import mdt.model.MDTExceptionEntity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASRESTfulClient {
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	protected final Lazy<JsonSerializer> m_ser = Lazy.of(JsonSerializer::new);
	protected final Lazy<JsonDeserializer> m_deser = Lazy.of(JsonDeserializer::new);
	
	public HttpAASRESTfulClient(OkHttpClient client) {
		m_client = client;
	}
	
	public OkHttpClient getHttpClient() {
		return m_client;
	}
	
	public <T> T parseJson(String jsonStr, Class<T> valueCls) throws DeserializationException {
		return m_deser.get().read(jsonStr, valueCls);
	}
	public <T> List<T> parseListJson(String jsonStr, Class<T> valueCls) throws DeserializationException {
		return m_deser.get().readList(jsonStr, valueCls);
	}
	
	public String writeJson(Object obj) throws SerializationException {
		return (obj instanceof String) ? (String)obj : m_ser.get().write(obj);
	}
	
	public RequestBody createRequestBody(Object desc) throws SerializationException {
		return RequestBody.create(writeJson(desc), JSON);
	}

	public <T> T call(Request req, Class<T> responseCls) throws RuntimeException {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, responseCls);
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	public <T> List<T> callList(Request req, Class<T> responseCls) throws RuntimeException {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseListResponse(resp, responseCls);
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}

	public void send(Request req) throws RuntimeException {
		try {
			Response resp =  m_client.newCall(req).execute();
			if ( !resp.isSuccessful() ) {
				throwErrorResponse(resp.body().string());
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T parseResponse(Response resp, Class<T> responseCls)
		throws MDTClientException, RuntimeException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				if ( respBody.length() == 0 ) {
					return null;
				}
				else if ( String.class.isAssignableFrom(responseCls)  ) {
					return (T)respBody;
				}
				try {
					return parseJson(respBody, responseCls);
				}
				catch ( DeserializationException e ) {
					String details = String.format("JSON-deserialization error, body=%s", respBody);
					throw new MDTClientException(details);
				}
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	private <T> List<T> parseListResponse(Response resp, Class<T> responseCls)
		throws MDTClientException, RuntimeException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				if ( respBody.length() == 0 ) {
					return null;
				}
				try {
					return parseListJson(respBody, responseCls);
				}
				catch ( DeserializationException e ) {
					String.format("JSON-deserialization error, body=%s", respBody);
					throw new MDTClientException(resp.toString());
				}
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void throwErrorResponse(String respBody) throws MDTClientException, RuntimeException {
		MDTExceptionEntity entity = null;
		try {
			entity = parseJson(respBody, MDTExceptionEntity.class);
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
}
