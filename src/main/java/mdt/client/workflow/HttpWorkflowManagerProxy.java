package mdt.client.workflow;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.func.FOption;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.RESTfulIOException;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.model.MDTWorkflowManager;
import mdt.workflow.model.WorkflowDescriptor;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpWorkflowManagerProxy implements MDTWorkflowManager, HttpClientProxy {
	private final HttpRESTfulClient m_restfulClient;
	private final JsonMapper m_mapper;
	
	private HttpWorkflowManagerProxy(Builder builder) {
		m_mapper = FOption.getOrElse(builder.m_mapper,
										() -> JsonMapper.builder().findAndAddModules().build());

		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(builder.m_httpClient)
										.endpoint(builder.m_endpoint)
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(m_mapper))
										.build();
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_restfulClient.getEndpoint();
	}

	@Override
	public WorkflowDescriptor getWorkflowDescriptor(String id) throws ResourceNotFoundException {
		String url = String.format("%s/descriptors/%s", getEndpoint(), id);
		return m_restfulClient.get(url, m_descDeser);
	}

	@Override
	public String getArgoWorkflowDescriptor(String id, String clientImageName) throws ResourceNotFoundException {
		String url = String.format("%s/descriptors/%s/argo?client-image=%s", getEndpoint(), id, clientImageName);
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public List<WorkflowDescriptor> getWorkflowDescriptorAll() {
		String url = String.format("%s/descriptors", getEndpoint());
		return m_restfulClient.get(url, m_descListDeser);
	}

	@Override
	public String addWorkflowDescriptor(WorkflowDescriptor desc) throws ResourceAlreadyExistsException {
		String url = String.format("%s/descriptors", getEndpoint());
		RequestBody reqBody = RequestBody.create(toJson(desc), HttpRESTfulClient.MEDIA_TYPE_JSON);
		
		return m_restfulClient.post(url, reqBody, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public void removeWorkflowDescriptor(String id) throws ResourceNotFoundException {
		String url = String.format("%s/descriptors/%s", getEndpoint(), id);
		m_restfulClient.delete(url);
	}

	@Override
	public void removeWorkflowDescriptorAll() {
		String url = String.format("%s/descriptors", getEndpoint());
		m_restfulClient.delete(url);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private JsonMapper m_mapper;
		
		private Builder() { }
		
		public HttpWorkflowManagerProxy build() {
			Preconditions.checkState(m_endpoint != null, "WorkflowManager's endpoint has not been set");
			
			m_httpClient = FOption.getOrElse(m_httpClient, OkHttpClient::new);
			m_mapper = FOption.getOrElse(m_mapper, MDTModelSerDe::getJsonMapper);
			
			return new HttpWorkflowManagerProxy(this);
		}
		
		public Builder httpClient(OkHttpClient client) {
			m_httpClient = client;
			return this;
		}
		
		public Builder endpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
		
		public Builder jsonMapper(JsonMapper mapper) {
			m_mapper = mapper;
			return this;
		}
	}
	
	private String toJson(Object obj) {
		try {
			return m_mapper.writeValueAsString(obj);
		}
		catch ( JsonProcessingException e ) {
			throw new RESTfulIOException(e);
		}
	}
	
	private ResponseBodyDeserializer<WorkflowDescriptor> m_descDeser = new ResponseBodyDeserializer<>() {
		@Override
		public WorkflowDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return m_mapper.readValue(respBody, WorkflowDescriptor.class);
		}
	};
	
	private ResponseBodyDeserializer<List<WorkflowDescriptor>> m_descListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<WorkflowDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return m_mapper.readerForListOf(WorkflowDescriptor.class).readValue(respBody);
		}
	};
}
