package mdt.client.operation;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import utils.LoggerSettable;
import utils.func.Nullables;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.client.HttpRESTfulClientOld;

/**
 * HTTP 기반 시뮬레이션 서버에 시뮬레이션 시작/상태 조회/취소를 요청하는 클라이언트.
 * <p>
 * 각 요청은 동기적으로 전송되며, 서버의 응답은 {@link OperationStatusResponse}로 변환되어 반환된다.
 * 시작 요청의 응답으로 받은 {@code operationLocation}은
 * 이후 상태 조회 및 취소 요청의 시뮬레이션 핸들로 사용된다.
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
	
	/**
	 * 주어진 HTTP 클라이언트와 시뮬레이션 서버 endpoint로 클라이언트를 생성한다.
	 *
	 * @param client	HTTP 클라이언트.
	 * @param endpoint	시뮬레이션 서버 endpoint URL.
	 */
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
	
	/**
	 * 주어진 JSON 파라미터로 시뮬레이션 시작을 요청한다.
	 * <p>
	 * endpoint로 POST 요청을 보내며, 응답 본문에 {@code operationLocation}이 없고 응답 헤더에
	 * {@code Location}이 있으면 그 값을 {@code operationLocation}으로 설정한다.
	 *
	 * @param paramtersJson	시뮬레이션 파라미터 JSON 문자열.
	 * @return	시뮬레이션 시작 요청에 대한 응답.
	 * @throws RESTfulIOException	서버에 접속하지 못했거나, 응답을 해석하지 못했거나, 요청 전송 중 입출력 오류가
	 * 								발생한 경우.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	public OperationStatusResponse<Void> startSimulation(String paramtersJson) {
		RequestBody body = HttpRESTfulClient.newJsonRequestBody(paramtersJson);
		Request req = new Request.Builder().url(getEndpoint()).post(body).build();
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending Simulation start request: url={}, method={}, body={}",
							getEndpoint(), req.method(), paramtersJson);
		}
		try ( Response resp = m_restClient.getHttpClient().newCall(req).execute() ) {
			OperationStatusResponse<Void> statusResp = m_restClient.parseResponse(resp, RESPONSE_TYPE_REF);
			if ( statusResp.getOperationLocation() == null && resp.header("Location") != null ) {
				statusResp.setOperationLocation(resp.header("Location"));
			}
			
			return statusResp;
		}
		catch ( SocketTimeoutException | ConnectException e ) {
			throw new RESTfulIOException("Failed to connect to the server: endpoint=" + getEndpoint(), e);
		}
		catch ( IOException e ) {
			throw new RESTfulIOException("Failed to send simulation start request: endpoint=" + getEndpoint(), e);
		}
	}
	
	/**
	 * 시뮬레이션 대상 Submodel의 식별자를 지정하여 시뮬레이션 시작을 요청한다.
	 * <p>
	 * {@code {"submodelId": <submodelId>}} 형태의 파라미터로 {@link #startSimulation(String)}을 호출한다.
	 *
	 * @param submodelId	시뮬레이션 Submodel 식별자.
	 * @return	시뮬레이션 시작 요청에 대한 응답.
	 */
	public OperationStatusResponse<Void> startSimulationWithSumodelId(String submodelId) {
		String paramJson = String.format("{ \"submodelId\": \"%s\"}", submodelId);
		return startSimulation(paramJson);
	}
	
	/**
	 * 시뮬레이션 대상 Submodel의 endpoint를 지정하여 시뮬레이션 시작을 요청한다.
	 * <p>
	 * {@code {"submodelEndpoint": <submodelEndpoint>}} 형태의 파라미터로
	 * {@link #startSimulation(String)}을 호출한다.
	 *
	 * @param submodelEndpoint	시뮬레이션 Submodel의 endpoint URL.
	 * @return	시뮬레이션 시작 요청에 대한 응답.
	 */
	public OperationStatusResponse<Void> startSimulationWithEndpoint(String submodelEndpoint) {
		String paramJson = String.format("{ \"submodelEndpoint\": \"%s\"}", submodelEndpoint);
		return startSimulation(paramJson);
	}
	
	/**
	 * 시뮬레이션의 현재 상태를 조회한다.
	 * <p>
	 * {@code <endpoint>/<simulationHandle>}로 GET 요청을 보낸다. {@code simulationHandle}이
	 * {@code null}이거나 공백 문자열이면 endpoint 자체로 요청한다.
	 *
	 * @param simulationHandle	시작 요청 응답의 {@code operationLocation} 값.
	 * @return	시뮬레이션 상태 응답.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 */
	public OperationStatusResponse<Void> statusSimulation(String simulationHandle) {
		String loc = Nullables.mapOrElse(simulationHandle, String::trim, "");
		String url = (loc.length() > 0) ?  String.format("%s/%s", getEndpoint(), loc) : getEndpoint();

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (GET) {}", url);
		}
		Request req = new Request.Builder().url(url).get().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
	
	/**
	 * 시뮬레이션 취소를 요청한다.
	 * <p>
	 * {@code <endpoint>/<simulationId>}로 DELETE 요청을 보낸다.
	 *
	 * @param simulationId	취소할 시뮬레이션 핸들 ({@code operationLocation} 값).
	 * @return	취소 요청에 대한 응답.
	 * @throws RESTfulRemoteException	서버가 오류를 응답한 경우.
	 * @throws RESTfulIOException	서버 접속 또는 응답 해석에 실패한 경우.
	 */
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
		return Nullables.orElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
		m_restClient.setLogger(logger);
	}
}
