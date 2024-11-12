package mdt.client.operation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import utils.LoggerSettable;
import utils.func.FOption;
import utils.http.HttpClientProxy;
import utils.http.RESTfulRemoteException;

import mdt.client.HttpRESTfulClientOld;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSimulationClient implements HttpClientProxy, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpSimulationClient.class);
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final TypeReference<OperationStatusResponse<Void>> RESPONSE_TYPE_REF
													= new TypeReference<OperationStatusResponse<Void>>(){};
	
	private final HttpRESTfulClientOld m_restClient;
	private Logger m_logger;
	
	public HttpSimulationClient(OkHttpClient client, String endpoint) {
		m_restClient = new HttpRESTfulClientOld(client, endpoint);
		
		m_restClient.setLogger(getLogger());
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_restClient.getEndpoint();
	}
	
	public OperationStatusResponse<Void> startSimulation(String paramtersJson) {
		RequestBody body = RequestBody.create(paramtersJson, JSON);
		Request req = new Request.Builder().url(getEndpoint()).post(body).build();
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending Simulation start request: url={}, method={}, body={}",
							getEndpoint(), req.method(), paramtersJson);
		}
		try {
			Response resp =  m_restClient.getHttpClient().newCall(req).execute();
			OperationStatusResponse<Void> statusResp = m_restClient.parseResponse(resp, RESPONSE_TYPE_REF);
			if ( statusResp.getOperationLocation() == null && resp.header("Location") != null ) {
				statusResp.setOperationLocation(resp.header("Location"));
			}
			
			return statusResp;
		}
		catch ( IOException e ) {
			throw new RESTfulRemoteException("" + e);
		}
	}
	
	public OperationStatusResponse<Void> startSimulationWithSumodelId(String submodelId) {
		String paramJson = String.format("{ \"submodelId\": \"%s\"}", submodelId);
		return startSimulation(paramJson);
	}
	
	public OperationStatusResponse<Void> startSimulationWithEndpoint(String submodelEndpoint) {
		String paramJson = String.format("{ \"submodelEndpoint\": \"%s\"}", submodelEndpoint);
		return startSimulation(paramJson);
	}
	
	public OperationStatusResponse<Void> statusSimulation(String simulationHandle) {
		String loc = FOption.mapOrElse(simulationHandle, String::trim, "");
		String url = (loc.length() > 0) ?  String.format("%s/%s", getEndpoint(), loc) : getEndpoint();

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (GET) {}", url);
		}
		Request req = new Request.Builder().url(url).get().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
	
	public OperationStatusResponse<Void> cancelSimulation(String simulationId) {
		String url = String.format("%s/%s", getEndpoint(), simulationId);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (DELETE) {}", url);
		}
		Request req = new Request.Builder().url(url).delete().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
		m_restClient.setLogger(logger);
	}
}
