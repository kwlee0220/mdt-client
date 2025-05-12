package mdt.client;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import utils.InternalException;
import utils.Throwables;
import utils.Utilities;
import utils.func.FOption;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.OkHttpClientUtils;
import utils.io.FileUtils;

import mdt.aas.ShellRegistry;
import mdt.aas.SubmodelRegistry;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.client.registry.HttpShellRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.workflow.HttpServiceClientFactoryRegistry;
import mdt.client.workflow.HttpWorkflowManager;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.workflow.WorkflowManager;
import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTManager implements MDTManager, HttpClientProxy {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpMDTManager.class);

//	private static final String RESTFUL_API_VERSION = "/api/v1.0";
	private static final String RESTFUL_API_VERSION = "";
	private static final String CLIENT_CONFIG_FILE = "mdt_client_config.yaml";
	private static final String INSTANCE_MANAGER_SUFFIX = "/instance-manager";
	private static final String SHELL_REGISTRY_SUFFIX = "/shell-registry";
	public static final String SUBMODEL_REGISTRY_SUFFIX = "/submodel-registry";
	private static final String WORKFLOW_MANAGER_SUFFIX = "/workflow-manager";
	
	private final HttpServiceClientFactoryRegistry m_serviceFactoryRegistry;
	
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	private MqttClient m_mqttClient = null;
	private final String m_wfMgrEndpoint;
	
	private HttpMDTManager(Builder builder) {
		m_endpoint = builder.m_endpoint;
		m_wfMgrEndpoint = builder.m_wfMgrEndpoint;
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(builder.m_httpClient)
										.jsonMapper(MDTModelSerDe.getJsonMapper())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(builder.m_mapper))
										.build();
		
		if ( builder.m_mqttEndpoint != null ) {
			subscribeMqttBroker(builder.m_mqttEndpoint);
		}
		
		m_serviceFactoryRegistry = new HttpServiceClientFactoryRegistry(builder.m_httpClient);
		m_serviceFactoryRegistry.register(ShellRegistry.class, m_endpoint + SHELL_REGISTRY_SUFFIX);
		m_serviceFactoryRegistry.register(SubmodelRegistry.class, m_endpoint + SUBMODEL_REGISTRY_SUFFIX);
		m_serviceFactoryRegistry.register(WorkflowManager.class, m_endpoint + WORKFLOW_MANAGER_SUFFIX);
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	public static HttpMDTManager connectWithDefault() {
		// 환경변수 MDT_CLIENT_HOME이 설정된 경우 해당 디렉토리에 'mdt_client_config.yaml' 파일이
		// 존재하면 이를 통해 MDTManager에 접속한다.
		// 환경변수 MDT_CLIENT_HOME가 존재하지 않는 경우에는 현재 working 디렉토리에서
		// 'mdt_client_config.yaml' 파일을 확인한다.

		File clientHomeDir = Utilities.getEnvironmentVariableFile("MDT_CLIENT_HOME")
										.getOrElse(FileUtils.getCurrentWorkingDirectory());
		File clientConfigFile = FileUtils.path(clientHomeDir, CLIENT_CONFIG_FILE);
		if ( clientConfigFile.canRead() ) {
			try {
				MDTClientConfig config = MDTClientConfig.load(clientConfigFile);
				return connect(config);
			}
			catch ( Throwable e ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				String msg = String.format("Failed to read client-config-file (%s), cause=%s", clientConfigFile, cause);
				s_logger.error(msg);
				
				throw new InternalException(msg, cause);
			}
		}
		
		// client config file이 존재하지 않는 경우에는 환경변수 MDT_ENDPOINT에 기록된
		// endpoint 정보를 사용하여 접속을 시도한다.
		String endpoint = System.getenv("MDT_ENDPOINT");
		if ( endpoint == null ) {
			throw new IllegalStateException("MDTInstanceManager's endpoint is missing");
		}
		
		return connect(endpoint);
	}
	
	public static HttpMDTManager connect(String endpoint) {
		return HttpMDTManager.builder()
									.endpoint(endpoint)
									.readTimeout(Duration.ofSeconds(30))
									.jsonMapper(MDTModelSerDe.getJsonMapper())
									.build();
	}
	
	public static HttpMDTManager connect(MDTClientConfig clientConfig) {
		HttpMDTManager.Builder builder = HttpMDTManager.builder()
																	.endpoint(clientConfig.getMdtEndpoint())
																	.jsonMapper(MDTModelSerDe.getJsonMapper());
		if ( clientConfig.getConnectTimeout() != null ) {
			builder = builder.connectTimeout(clientConfig.getConnectTimeout());
		}
		if ( clientConfig.getReadTimeout() != null ) {
			builder = builder.readTimeout(clientConfig.getReadTimeout());
		}
		if ( clientConfig.getMqttEndpoint() != null ) {
			builder = builder.mqttEndpoint(clientConfig.getMqttEndpoint());
		}
		
		if ( clientConfig.getWorkflowManagerEndpoint() != null ) {
            builder = builder.workflowManagerEndpoint(clientConfig.getWorkflowManagerEndpoint());
		}
		else {
			builder = builder.workflowManagerEndpoint(clientConfig.getMdtEndpoint());
		}
		
		return builder.build();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(Class<T> svcClass) {
		if ( svcClass.isAssignableFrom(HttpMDTInstanceManager.class) ) {
			return (T)getInstanceManager();
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
	 * MDTManager가 사용하는 {@link ShellRegistry} proxy 객체를 반환한다.
	 * 
	 * @return	{@link ShellRegistry} proxy 객체
	 */
	public HttpShellRegistryClient getAssetAdministrationShellRegistry() {
		String endpoint = String.format("%s%s", getEndpoint(), SHELL_REGISTRY_SUFFIX);
		return new HttpShellRegistryClient(getHttpClient(), endpoint);
	}

	/**
	 * MDTManager가 사용하는 {@link SubmodelRegistry} proxy 객체를 반환한다.
	 * 
	 * @return	{@link SubmodelRegistry} proxy 객체
	 */
	public SubmodelRegistry getSubmodelRegistry() {
		String endpoint = String.format("%s%s", getEndpoint(), SUBMODEL_REGISTRY_SUFFIX);
		return new HttpSubmodelRegistryClient(m_restfulClient.getHttpClient(), endpoint);
//		return createClient(SubmodelRegistry.class);
		
	}
	
	public HttpMDTInstanceManager getInstanceManager() {
		String endpoint = String.format("%s%s", getEndpoint(), INSTANCE_MANAGER_SUFFIX);
		return HttpMDTInstanceManager.builder()
											.httpClient(getHttpClient())
											.endpoint(endpoint)
											.build();
	}
	
	public WorkflowManager getWorkflowManager() {
//		return createClient(WorkflowModelManager.class);
		return new HttpWorkflowManager(getHttpClient(), m_wfMgrEndpoint + WORKFLOW_MANAGER_SUFFIX);
	}
	
	public <T> T createClient(Class<T> serviceClass) {
		return m_serviceFactoryRegistry.createClient(serviceClass);
	}
	
	public static void register(Object subscriber) {
		EVENT_BUS.register(subscriber);
	}

    // @GetMapping({"/ping"})
	public void ping() {
		String url = String.format("%s/ping", getEndpoint());
		m_restfulClient.get(url);
	}

    // @DeleteMapping({"/shutdown"})
	public void shutdown() {
//		String url = String.format("%s/shutdown", getEndpoint());
//		m_restfulClient.delete(url);
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private Duration m_connectTimeout;
		private Duration m_readTimeout;
		private String m_mqttEndpoint;
		private JsonMapper m_mapper;
		private String m_wfMgrEndpoint;
		
		private Builder() { }
		
		public HttpMDTManager build() {
			Preconditions.checkState(m_endpoint != null, "MDTManager endpoint has not been set");

			try {
				OkHttpClient.Builder builder = OkHttpClientUtils.newTrustAllOkHttpClientBuilder();
				if ( m_connectTimeout != null ) {
					builder = builder.connectTimeout(m_connectTimeout);
				}
				if ( m_readTimeout != null ) {
					builder = builder.readTimeout(m_readTimeout);
				}
				m_httpClient = builder.build();
				m_mapper = FOption.getOrElse(m_mapper, MDTModelSerDe::getJsonMapper);
			}
			catch ( KeyManagementException | NoSuchAlgorithmException e ) {
				throw new InternalException("Failed to create HttpClient", e);
			}
			
			return new HttpMDTManager(this);
		}
		
		public Builder endpoint(String endpoint) {
			if ( !endpoint.contains("/api/v") ) {
				endpoint = endpoint + RESTFUL_API_VERSION;
			}
			
			m_endpoint = endpoint;
			if ( m_wfMgrEndpoint == null ) {
                m_wfMgrEndpoint = endpoint;
            }
			return this;
		}
		
		public Builder connectTimeout(Duration to) {
			m_connectTimeout = to;
			return this;
		}
		
		public Builder readTimeout(Duration to) {
			m_readTimeout = to;
			return this;
		}
		
		public Builder mqttEndpoint(String endpoint) {
			m_mqttEndpoint = endpoint;
			return this;
		}
		
		public Builder jsonMapper(JsonMapper mapper) {
			m_mapper = mapper;
			return this;
		}
		
		public Builder workflowManagerEndpoint(String endpoint) {
			m_wfMgrEndpoint = endpoint;
			return this;
		}
	}

	private static final String TOPIC_PATTERN = "/mdt/manager/instances/#";
	private static EventBus EVENT_BUS = new EventBus();
	
	private void subscribeMqttBroker(String endpoint) {
		try {
			MqttConnectOptions opts = new MqttConnectOptions();
			opts.setCleanSession(true);
			opts.setKeepAliveInterval(30);
			int qos = 0;
			
			String clientId = UUID.randomUUID().toString();
			m_mqttClient = new MqttClient(endpoint, clientId, new MemoryPersistence());
			m_mqttClient.setCallback(null);
			m_mqttClient.connect(opts);
			
			m_mqttClient.subscribe(TOPIC_PATTERN, qos, (topic, msg) -> {
				EVENT_BUS.post(new MDTManagerMqttMessage(topic, msg));
			});
		}
		catch ( MqttException ignored ) {
			s_logger.warn("Failed to open an MQTT connection", ignored);
		}
	}
}
