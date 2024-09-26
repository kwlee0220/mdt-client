package mdt.client.workflow;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.func.FOption;

import mdt.client.HttpRESTfulClient;
import mdt.client.SSLUtils;
import mdt.model.AASUtils;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.model.workflow.MDTWorkflowManager;
import mdt.model.workflow.MDTWorkflowManagerException;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpWorkflowManagerClient extends HttpRESTfulClient implements MDTWorkflowManager {
	private static final TypeReference<String> STRING_TYPE = new TypeReference<String>(){};
	private static final TypeReference<WorkflowDescriptor> DESC_TYPE
														= new TypeReference<WorkflowDescriptor>(){};
	private static final TypeReference<List<WorkflowDescriptor>> DESC_LIST_TYPE
														= new TypeReference<List<WorkflowDescriptor>>(){};
	private static final TypeReference<TaskTemplateDescriptor> TASK_TEMPLATE_TYPE
														= new TypeReference<TaskTemplateDescriptor>(){};
	private static final TypeReference<List<TaskTemplateDescriptor>> TASK_TEMPLATE_LIST_TYPE
														= new TypeReference<List<TaskTemplateDescriptor>>(){};
	
	private HttpWorkflowManagerClient(Builder builder) {
		super(builder.m_httpClient, builder.m_endpoint, builder.m_mapper);
	}

	@Override
	public WorkflowDescriptor getWorkflowDescriptor(String id) throws ResourceNotFoundException {
		String url = String.format("%s/descriptors/%s", getEndpoint(), id);

		Request req = new Request.Builder().url(url).get().build();
		return call(req, DESC_TYPE);
	}

	@Override
	public List<WorkflowDescriptor> getWorkflowDescriptorAll() {
		String url = String.format("%s/descriptors", getEndpoint());

		Request req = new Request.Builder().url(url).get().build();
		return call(req, DESC_LIST_TYPE);
	}

	@Override
	public String addWorkflowDescriptor(WorkflowDescriptor desc) throws ResourceAlreadyExistsException {
		String url = String.format("%s/descriptors", getEndpoint());
		RequestBody reqBody = createRequestBody(desc); 

		Request req = new Request.Builder().url(url).post(reqBody).build();
		return call(req, STRING_TYPE);
	}

	@Override
	public void removeWorkflowDescriptor(String id) throws ResourceNotFoundException {
		String url = String.format("%s/descriptors/%s", getEndpoint(), id);

		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	@Override
	public void removeWorkflowDescriptorAll() {
		String url = String.format("%s/descriptors", getEndpoint());

		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	@Override
	public TaskTemplateDescriptor getBuiltInTaskTemplate(String id) throws ResourceNotFoundException {
		String url = String.format("%s/builtin-tasks/%s", getEndpoint(), id);

		Request req = new Request.Builder().url(url).get().build();
		return call(req, TASK_TEMPLATE_TYPE);
	}
	
	@Override
	public Collection<TaskTemplateDescriptor> getBuiltInTaskTemplateAll() {
		String url = String.format("%s/builtin-tasks", getEndpoint());

		Request req = new Request.Builder().url(url).get().build();
		return call(req, TASK_TEMPLATE_LIST_TYPE);
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private JsonMapper m_mapper;
		
		private Builder() { }
		
		public HttpWorkflowManagerClient build() {
			Preconditions.checkState(m_endpoint != null, "WorkflowManager's endpoint has not been set");
			
			if ( m_httpClient == null ) {
				try {
					m_httpClient = SSLUtils.newTrustAllOkHttpClientBuilder().build();
				}
				catch ( Exception e ) {
					throw new MDTWorkflowManagerException("" + e);
				}
			}
			m_mapper = FOption.getOrElse(m_mapper, AASUtils::getJsonMapper);
			
			return new HttpWorkflowManagerClient(this);
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
}
