package mdt.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.func.Tuple;

import mdt.model.registry.RegistryException;
import mdt.model.registry.RegistryExceptionEntity;
import mdt.model.registry.ResourceNotFoundException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Fa3stHttpClient {
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	private final JsonSerializer m_ser;
	private final JsonDeserializer m_deser;
	
	public Fa3stHttpClient(OkHttpClient client) {
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
			throw new MDTClientException("" + e);
		}
	}

	protected <T> Tuple<String,T> callAsync(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			T result = parseResponse(resp, resultCls);
			if ( resp.code() == 202 ) {
				return Tuple.of(resp.header("Location"), result);
			}
			else {
				return Tuple.of(null, result);
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	protected <T> List<T> callList(Request req, Class<T> resultCls) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponseToList(resp, resultCls);
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}

	protected void send(Request req) {
		try {
			Response resp =  m_client.newCall(req).execute();
			if ( !resp.isSuccessful() ) {
				throwErrorResponse(resp, resp.body().string());
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	private <T> T parseResponse(Response resp, Class<T> valueType)
		throws RegistryException, MDTClientException {
		try {
			if ( resp.isSuccessful() ) {
				if ( resp.code() != 204 ) {
					String respBody = resp.body().string();
					if ( respBody.length() > 0 ) {
						return m_deser.read(respBody, valueType);
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}
			else {
				String respBody = resp.body().string();
				throwErrorResponse(resp, respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException | DeserializationException e ) {
			throw new MDTClientException(e.toString());
		}
	}
	
	private <T> List<T> parseResponseToList(Response resp, Class<T> valueType)
		throws RegistryException, MDTClientException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				JsonMapper mapper = JsonMapper.builder().build();
				JsonNode root = mapper.readTree(respBody);
				JsonNode result = root.path("result");
				return m_deser.readList(result, valueType);
			}
			else {
				throwErrorResponse(resp, respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException | DeserializationException e ) {
			throw new MDTClientException(resp.toString());
		}
	}
	
	public static final class Messages {
		@JsonProperty("messages") 
		private List<RegistryExceptionEntity> m_messages;
	}
	
	private void throwErrorResponse(Response resp, String respBody)
		throws RegistryException, MDTClientException {
		RegistryExceptionEntity msg = null;
		
		try {
			Messages msgs = m_deser.read(respBody, Messages.class);
			msg = msgs.m_messages.get(0);
			if ( msg.getCode().length() == 0 ) {
				if ( resp.code() == 404 ) {
					throw new ResourceNotFoundException(msg.getText());
				}
			}
			
			Class<? extends Throwable> cls = (Class<? extends Throwable>) Class.forName(msg.getCode());
			Constructor<? extends Throwable> ctor = cls.getConstructor(String.class);
			throw (RuntimeException)ctor.newInstance(msg.getText());
		}
		catch ( RegistryException e ) { throw e; }
		catch ( MDTClientException e ) { throw e; }
		catch ( Exception e ) {
			String details = ( msg != null )
							? msg.getCode() + ": " + msg.getText() + ", ts=" + msg.getTimestamp()
							: respBody;
			throw new MDTClientException(details);
		}
	}
}
