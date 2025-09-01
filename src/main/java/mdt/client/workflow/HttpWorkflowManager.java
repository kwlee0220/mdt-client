package mdt.client.workflow;

import java.io.IOException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;

import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.model.AASUtils;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpWorkflowManager implements WorkflowManager, HttpClientProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	public HttpWorkflowManager(OkHttpClient client, String endpoint) {
		Preconditions.checkArgument(endpoint != null, "HttpWorkflowManager: endpoint is null");
		
		m_endpoint = endpoint;
		ErrorEntityDeserializer errorDeser = new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER);
		m_restfulClient = HttpRESTfulClient.builder()
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
	public Workflow startWorkflow(@NonNull String wfModelId) throws ResourceNotFoundException {
		Preconditions.checkArgument(wfModelId != null, "WorkflowModel id is null");
		
		String url = String.format("%s/models/%s/start", m_endpoint, wfModelId);
		
		RequestBody reqBody = RequestBody.create("", HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.post(url, reqBody, m_wfDeser);
	}

	@Override
	public void stopWorkflow(String wfName) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s/stop", m_endpoint, wfName);
		
		RequestBody reqBody = RequestBody.create("", HttpRESTfulClient.MEDIA_TYPE_JSON);
		m_restfulClient.put(url, reqBody);
	}

	@Override
	public Workflow suspendWorkflow(String wfName) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s/suspend", m_endpoint, wfName);
		
		RequestBody reqBody = RequestBody.create("", HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.put(url, reqBody, m_wfDeser);
	}

	@Override
	public Workflow resumeWorkflow(String wfName) throws ResourceNotFoundException {
		String url = String.format("%s/workflows/%s/resume", m_endpoint, wfName);
		
		RequestBody reqBody = RequestBody.create("", HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.put(url, reqBody, m_wfDeser);
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

	@Override
	public String getWorkflowScript(String id, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException {
		StringBuilder builder = new StringBuilder();
		if ( mdtEndpoint != null ) {
			builder.append("mdt-endpoint=")
					.append(AASUtils.encodeBase64UrlSafe(mdtEndpoint));
		}
		if ( clientDockerImage != null ) {
			if ( builder.length() > 0 ) {
				builder.append("&");
			}
			builder.append("client-docker-image=")
					.append(AASUtils.encodeBase64UrlSafe(clientDockerImage));
		}
		
		String paramsStr = builder.length() > 0 ? "?" + builder.toString() : "";
		String url = String.format("%s/models/%s/script%s", m_endpoint, id, paramsStr);
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public WorkflowModel addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		String url = String.format("%s/models", m_endpoint);
		
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = RequestBody.create(requestJson, HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.post(url, reqBody, m_wfModelDeser);
	}

	@Override
	public WorkflowModel addOrReplaceWorkflowModel(WorkflowModel desc) {
		String url = String.format("%s/models?updateIfExists=true", m_endpoint);
		
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = RequestBody.create(requestJson, HttpRESTfulClient.MEDIA_TYPE_JSON);
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
