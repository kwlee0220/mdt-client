package mdt.client;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.func.FOption;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.registry.HttpShellRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.workflow.HttpWorkflowManagerClient;
import mdt.model.AASUtils;
import mdt.model.MDTManager;
import mdt.model.MDTService;
import mdt.model.registry.AASRegistry;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.workflow.MDTWorkflowManagerException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTManagerClient extends HttpRESTfulClient implements MDTManager, HttpClientProxy {
	private static final String INSTANCE_MANAGER_SUFFIX = "/instance-manager";
	private static final String WORKFLOW_MANAGER_SUFFIX = "/workflow-manager";
	private static final String AAS_REGISTRY_SUFFIX = "/shell-registry";
	public static final String SUBMODEL_REGISTRY_SUFFIX = "/submodel-registry";
	
	private HttpMDTManagerClient(Builder builder) {
		super(builder.m_httpClient, builder.m_endpoint, builder.m_mapper);
	}
	
	public static HttpMDTManagerClient connect(String endpoint) {
		return HttpMDTManagerClient.builder()
									.endpoint(endpoint)
									.jsonMapper(AASUtils.getJsonMapper())
									.build();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends MDTService> T getService(Class<T> svcClass) {
		if ( svcClass.isAssignableFrom(HttpMDTInstanceManagerClient.class) ) {
			return (T)getInstanceManager();
		}
		else if ( svcClass.isAssignableFrom(HttpWorkflowManagerClient.class) ) {
			return (T)getWorkflowManager();
		}
		else if ( svcClass.isAssignableFrom(HttpShellRegistryClient.class) ) {
			return (T)getAssetAdministrationShellRegistry();
		}
		else if ( svcClass.isAssignableFrom(HttpSubmodelRegistryClient.class) ) {
			return (T)getSubmodelRegistry();
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * MDTManager가 사용하는 {@link AASRegistry} proxy 객체를 반환한다.
	 * 
	 * @return	{@link AASRegistry} proxy 객체
	 */
	public HttpShellRegistryClient getAssetAdministrationShellRegistry() {
		String endpoint = String.format("%s%s", getEndpoint(), AAS_REGISTRY_SUFFIX);
		return new HttpShellRegistryClient(getHttpClient(), endpoint);
	}

	/**
	 * MDTManager가 사용하는 {@link SubmodelRegistry} proxy 객체를 반환한다.
	 * 
	 * @return	{@link SubmodelRegistry} proxy 객체
	 */
	public HttpSubmodelRegistryClient getSubmodelRegistry() {
		String endpoint = String.format("%s%s", getEndpoint(), SUBMODEL_REGISTRY_SUFFIX);
		return new HttpSubmodelRegistryClient(getHttpClient(), endpoint);
	}
	
	public HttpMDTInstanceManagerClient getInstanceManager() {
		String endpoint = String.format("%s%s", getEndpoint(), INSTANCE_MANAGER_SUFFIX);
		return HttpMDTInstanceManagerClient.builder()
											.httpClient(getHttpClient())
											.endpoint(endpoint)
											.build();
	}
	
	public HttpWorkflowManagerClient getWorkflowManager() {
		String endpoint = String.format("%s%s", getEndpoint(), WORKFLOW_MANAGER_SUFFIX);
		return HttpWorkflowManagerClient.builder()
										.httpClient(getHttpClient())
										.endpoint(endpoint)
										.jsonMapper(AASUtils.getJsonMapper())
										.build();
	}

    // @GetMapping({"/ping"})
	public void ping() {
		String url = String.format("%s/ping", getEndpoint());
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

    // @GetMapping({"/shutdown"})
	public void shutdown() {
		String url = String.format("%s/shutdown", getEndpoint());
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private JsonMapper m_mapper;
		
		private Builder() { }
		
		public HttpMDTManagerClient build() {
			Preconditions.checkState(m_endpoint != null, "MDTManager endpoint has not been set");
			
			if ( m_httpClient == null ) {
				try {
					m_httpClient = SSLUtils.newTrustAllOkHttpClientBuilder().build();
				}
				catch ( Exception e ) {
					throw new MDTWorkflowManagerException("" + e);
				}
			}
			m_mapper = FOption.getOrElse(m_mapper, AASUtils::getJsonMapper);
			
			return new HttpMDTManagerClient(this);
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
