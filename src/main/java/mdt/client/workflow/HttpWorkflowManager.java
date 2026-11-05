package mdt.client.workflow;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.client.MDTRESTfulClients;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.WorkflowStatus;


/**
 * HTTP(REST)를 통해 원격 워크플로우 관리자(mdt-workflow)에 접근하는 {@link WorkflowManager} 클라이언트.
 * <p>
 * endpoint 하위의 {@code /models}로 워크플로우 모델을, {@code /workflows}로 워크플로우 인스턴스를 관리한다.
 * 모든 요청은 동기적으로 전송된다. 서버 오류는 {@link MDTRESTfulClients}에 등록된 MDT 예외 타입
 * (예: {@link ResourceNotFoundException}, {@link ResourceAlreadyExistsException}) 또는
 * {@link utils.http.RESTfulRemoteException}으로, 통신 오류는 {@link utils.http.RESTfulIOException}으로 전달된다.
 * <p>
 * {@link #listWorkflowIds()}와 {@link #getWorkflowStatus(String)}는 지원하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpWorkflowManager implements WorkflowManager, HttpClientProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	워크플로우 관리자 endpoint (예: {@code <mdt-url>/workflow-manager}).
	 * @throws IllegalArgumentException	{@code endpoint}가 {@code null}인 경우.
	 */
	public HttpWorkflowManager(OkHttpClient client, String endpoint) {
		Preconditions.checkArgument(endpoint != null, getClass().getSimpleName() + ": endpoint is null");
		
		m_endpoint = endpoint;
		ErrorEntityDeserializer errorDeser = new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER);
		m_restfulClient = MDTRESTfulClients.builder()
											.httpClient(client)
											.errorEntityDeserializer(errorDeser)
											.build();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	/**
	 * 지원하지 않는다.
	 *
	 * @return	정상적으로 반환되지 않는다.
	 * @throws UnsupportedOperationException	항상 발생된다.
	 */
	@Override
	public List<String> listWorkflowIds() {
		throw new UnsupportedOperationException("listWorkflowIds is not supported in HttpWorkflowManager");
	}

	/**
	 * 지원하지 않는다.
	 *
	 * @param wfIdStr	워크플로우 인스턴스 식별자.
	 * @return	정상적으로 반환되지 않는다.
	 * @throws UnsupportedOperationException	항상 발생된다.
	 */
	@Override
	public WorkflowStatus getWorkflowStatus(String wfIdStr) throws ResourceNotFoundException {
		throw new UnsupportedOperationException("getWorkflowStatus is not supported in HttpWorkflowManager");
	}

	@Override
	public List<Workflow> getWorkflowAll() {
		String url = String.format("%s/workflows", m_endpoint);
		return m_restfulClient.get(url, m_wfListDeser);
	}

	@Override
	public Workflow getWorkflow(String name) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s", m_endpoint, name);
		return m_restfulClient.get(url, m_wfDeser);
	}

	@Override
	public void removeWorkflow(String name) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s", m_endpoint, name);
		m_restfulClient.delete(url);
	}

	@Override
	public void removeWorkflowAll() {
		String url = String.format("%s/workflows", m_endpoint);
		m_restfulClient.delete(url);
	}

	@Override
	public Workflow startWorkflow(String wfModelId) throws ResourceNotFoundException {
		Preconditions.checkArgument(wfModelId != null, "WorkflowModel id is null");
		
		String url = String.format("%s/models/%s/start", m_endpoint, wfModelId);

		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody("");
		return m_restfulClient.post(url, reqBody, m_wfDeser);
	}

	@Override
	public void stopWorkflow(String wfName) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s/stop", m_endpoint, wfName);

		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody("");
		m_restfulClient.put(url, reqBody);
	}

	@Override
	public String getWorkflowLog(String wfName, String podName) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s/log/%s", m_endpoint, wfName, podName);
		
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public List<WorkflowModel> getWorkflowModelAll() {
		String url = String.format("%s/models", m_endpoint);
		return m_restfulClient.get(url, m_wfModelListDeser);
	}

	@Override
	public WorkflowModel getWorkflowModel(String id) throws ResourceNotFoundException {
		String url = String.format("%s/models/%s", m_endpoint, id);
		return m_restfulClient.get(url, m_wfModelDeser);
	}

	/**
	 * 주어진 워크플로우 모델로부터 생성된 워크플로우 스크립트를 반환한다.
	 * <p>
	 * {@code GET <endpoint>/models/<wfModelId>/script} 요청의 응답 본문을 그대로 반환한다.
	 *
	 * @param wfModelId	워크플로우 모델 식별자.
	 * @return	워크플로우 스크립트 문자열.
	 * @throws ResourceNotFoundException	식별자에 해당하는 워크플로우 모델이 존재하지 않는 경우.
	 */
	public String getWorkflowScript(String wfModelId) throws ResourceNotFoundException {
		String url = String.format("%s/models/%s/script", m_endpoint, wfModelId);
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public WorkflowModel addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		String url = String.format("%s/models", m_endpoint);
		
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(requestJson);
		return m_restfulClient.post(url, reqBody, m_wfModelDeser);
	}

	@Override
	public WorkflowModel addOrReplaceWorkflowModel(WorkflowModel desc) {
		String url = String.format("%s/models?updateIfExists=true", m_endpoint);
		
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = HttpRESTfulClient.newJsonRequestBody(requestJson);
		return m_restfulClient.post(url, reqBody, m_wfModelDeser);
	}

	@Override
	public void removeWorkflowModel(String id) throws ResourceNotFoundException {
		String url = String.format("%s/models/%s", m_endpoint, id);
		m_restfulClient.delete(url);
	}

	@Override
	public void removeWorkflowModelAll() {
		String url = String.format("%s/models", m_endpoint);
		m_restfulClient.delete(url);
	}
	
	@Override
	public String toString() {
		return String.format("HttpWorkflowManager: endpoint=%s", m_endpoint);
	}
	
	private ResponseBodyDeserializer<Workflow> m_wfDeser = new ResponseBodyDeserializer<>() {
		@Override
		public Workflow deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.getJsonMapper().readValue(respBody, Workflow.class);
		}
	};
	private static final TypeReference<List<Workflow>> WORKFLOW_LIST_TYPE = new TypeReference<List<Workflow>>() {};
	private ResponseBodyDeserializer<List<Workflow>> m_wfListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<Workflow> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.getJsonMapper().readValue(respBody, WORKFLOW_LIST_TYPE);
		}
	};
	
	private ResponseBodyDeserializer<WorkflowModel> m_wfModelDeser = new ResponseBodyDeserializer<>() {
		@Override
		public WorkflowModel deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.getJsonMapper().readValue(respBody, WorkflowModel.class);
		}
	};
	private ResponseBodyDeserializer<List<WorkflowModel>> m_wfModelListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<WorkflowModel> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, WorkflowModel.class);
		}
	};
}
