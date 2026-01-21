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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Fa3stHttpClient implements HttpClientProxy {
	private static final Logger s_logger = LoggerFactory.getLogger(Fa3stHttpClient.class);
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final String m_endpoint;
	private final OkHttpClient m_client;
//	private Supplier<AASFile> m_aasFileFactory = () -> MemoryAASFile.builder().build();
	
	public Fa3stHttpClient(OkHttpClient client, String endpoint) {
		m_endpoint = endpoint;
		m_client = client;
	}
	
	public OkHttpClient getHttpClient() {
		return m_client;
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	protected RequestBody createRequestBody(Object desc) throws IOException {
		if ( desc instanceof String str ) {
			return RequestBody.create(str, JSON);
		}
		else {
			String jsonStr = MDTModelSerDe.toJsonString(desc);
			return RequestBody.create(jsonStr, JSON);
		}
	}

	protected <T> T call(Request req, Class<T> resultCls) {
		return call(req, resultCls, null);
	}

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
