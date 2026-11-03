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

import okhttp3.OkHttpClient;

import utils.InternalException;
import utils.Throwables;
import utils.Utilities;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.OkHttpClientUtils;
import utils.io.FileUtils;
import utils.func.Nullables;

import mdt.aas.ShellRegistry;
import mdt.aas.SubmodelRegistry;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.client.registry.HttpShellRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.workflow.HttpServiceClientFactoryRegistry;
import mdt.client.workflow.HttpWorkflowManager;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.WorkflowManager;

/**
 * HTTP(REST)를 통해 원격 MDT Manager(mdt-manager)에 접속하는 {@link MDTManager} 구현.
 * <p>
 * MDTManager의 endpoint를 기준으로 다음 하위 서비스에 대한 클라이언트 객체를 제공한다.
 * <ul>
 * 	<li>{@code <endpoint>/instance-manager}: {@link HttpMDTInstanceManager}.
 * 	<li>{@code <endpoint>/aas_registry/shell-descriptors}: {@link HttpShellRegistryClient}.
 * 	<li>{@code <endpoint>/aas_registry/submodel-descriptors}: {@link HttpSubmodelRegistryClient}.
 * 	<li>{@code <workflow-manager endpoint>/workflow-manager}: {@link HttpWorkflowManager}.
 * </ul>
 * MQTT broker endpoint가 지정된 경우에는 생성 시 broker에 접속하여 {@code /mdt/manager/instances/#}
 * 토픽을 구독하고, 수신한 메시지를 {@link MDTManagerMqttMessage}로 감싸 JVM 전역 {@link EventBus}에
 * 게시한다. 이 이벤트는 {@link #register(Object)}로 등록한 구독자에게 전달된다.
 * <p>
 * 객체는 {@link #builder()} 또는 {@code connect*} 정적 메소드를 통해 생성한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTManager implements MDTManager, HttpClientProxy {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpMDTManager.class);

//	private static final String RESTFUL_API_VERSION = "/api/v1.0";
	private static final String RESTFUL_API_VERSION = "";
	private static final String CLIENT_CONFIG_FILE = "mdt_client_config.yaml";
	private static final String INSTANCE_MANAGER_SUFFIX = "/instance-manager";
	private static final String SHELL_REGISTRY_SUFFIX = "/aas_registry/shell-descriptors";
	/** MDTManager endpoint에 덧붙여 Submodel 레지스트리 endpoint를 구성하는 경로. */
	public static final String SUBMODEL_REGISTRY_SUFFIX = "/aas_registry/submodel-descriptors";
	private static final String WORKFLOW_MANAGER_SUFFIX = "/workflow-manager";
	
	private final HttpServiceClientFactoryRegistry m_serviceFactoryRegistry;
	
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	private MqttClient m_mqttClient = null;
	private final String m_wfMgrEndpoint;
	
	private HttpMDTManager(Builder builder) {
		m_endpoint = builder.m_endpoint;
		m_wfMgrEndpoint = builder.m_wfMgrEndpoint;
		m_restfulClient = MDTRESTfulClients.builder()
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
		m_serviceFactoryRegistry.register(WorkflowManager.class, m_wfMgrEndpoint + WORKFLOW_MANAGER_SUFFIX);
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	/**
	 * 기본 설정을 이용하여 MDTManager에 접속한다.
	 * <p>
	 * 환경변수 {@code MDT_CLIENT_HOME}이 가리키는 디렉토리(설정되지 않은 경우는 현재 작업 디렉토리)에
	 * {@code mdt_client_config.yaml} 파일이 존재하고 읽을 수 있으면 이 파일의 설정({@link MDTClientConfig})으로
	 * 접속한다. 파일이 없는 경우에는 환경변수 {@code MDT_URL}의 값을 endpoint로 사용하여 접속한다.
	 * <p>
	 * 실제 네트워크 접속 여부는 확인하지 않고 클라이언트 객체만 생성한다
	 * (단, MQTT broker endpoint가 설정된 경우 broker에는 접속을 시도한다).
	 *
	 * @return	생성된 {@link HttpMDTManager} 객체.
	 * @throws InternalException	설정 파일을 읽거나 해석하는 데 실패한 경우.
	 * @throws IllegalStateException	설정 파일이 없고 환경변수 {@code MDT_URL}도 설정되지 않은 경우.
	 */
	public static HttpMDTManager connectWithDefault() {
		// 환경변수 MDT_CLIENT_HOME이 설정된 경우 해당 디렉토리에 'mdt_client_config.yaml' 파일이
		// 존재하면 이를 통해 MDTManager에 접속한다.
		// 환경변수 MDT_CLIENT_HOME가 존재하지 않는 경우에는 현재 working 디렉토리에서
		// 'mdt_client_config.yaml' 파일을 확인한다.

		File clientHomeDir = Utilities.getEnvironmentVariableFile("MDT_CLIENT_HOME")
										.orElseGet(FileUtils::getCurrentWorkingDirectory);
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
		
		// client config file이 존재하지 않는 경우에는 환경변수 MDT_URL에 기록된
		// endpoint 정보를 사용하여 접속을 시도한다.
		String endpoint = System.getenv("MDT_URL");
		if ( endpoint == null ) {
			throw new IllegalStateException("MDTPlatform URL is missing");
		}
		
//		System.out.println("Connecting to MDTManager with URL: " + URL);
		
		return connect(endpoint);
	}
	
	/**
	 * 주어진 클라이언트 설정 파일을 이용하여 MDTManager에 접속한다.
	 *
	 * @param clientConfigFile	클라이언트 설정 파일 ({@link MDTClientConfig} 형식의 YAML 파일).
	 * @return	생성된 {@link HttpMDTManager} 객체.
	 * @throws IllegalArgumentException	{@code clientConfigFile}이 {@code null}인 경우.
	 * @throws InternalException	설정 파일을 읽거나 해석하는 데 실패한 경우.
	 * @see #connect(MDTClientConfig)
	 */
	public static HttpMDTManager connectWithConfigFile(File clientConfigFile) {
		Preconditions.checkArgument(clientConfigFile != null, "clientConfigFile is null");
		
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
	
	/**
	 * 주어진 endpoint의 MDTManager에 접속한다.
	 * <p>
	 * 읽기 제한시간은 30초로 설정되며, 워크플로우 관리자도 동일한 endpoint를 사용한다.
	 *
	 * @param endpoint	MDTManager endpoint.
	 * @return	생성된 {@link HttpMDTManager} 객체.
	 */
	public static HttpMDTManager connect(String endpoint) {
		return HttpMDTManager.builder()
							.endpoint(endpoint)
							.readTimeout(Duration.ofSeconds(30))
							.jsonMapper(MDTModelSerDe.getJsonMapper())
							.build();
	}
	
	/**
	 * 주어진 클라이언트 설정을 이용하여 MDTManager에 접속한다.
	 * <p>
	 * 설정의 접속/읽기 제한시간과 MQTT endpoint는 설정된 경우에만 적용된다.
	 * 워크플로우 관리자 URL이 설정되지 않은 경우에는 MDTManager URL을 사용한다.
	 *
	 * @param clientConfig	클라이언트 설정.
	 * @return	생성된 {@link HttpMDTManager} 객체.
	 */
	public static HttpMDTManager connect(MDTClientConfig clientConfig) {
		HttpMDTManager.Builder builder = HttpMDTManager.builder()
														.endpoint(clientConfig.getMdtUrl())
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
		
		if ( clientConfig.getWorkflowManagerUrl() != null ) {
            builder = builder.workflowManagerEndpoint(clientConfig.getWorkflowManagerUrl());
		}
		else {
			builder = builder.workflowManagerEndpoint(clientConfig.getMdtUrl());
		}
		
		return builder.build();
	}
	
	/**
	 * 주어진 서비스 클래스에 해당하는 서비스 클라이언트 객체를 반환한다.
	 * <p>
	 * {@link HttpMDTInstanceManager}, {@link HttpShellRegistryClient}, {@link HttpSubmodelRegistryClient}에
	 * 할당 가능한 클래스(예: {@link MDTInstanceManager}, {@link ShellRegistry}, {@link SubmodelRegistry})만
	 * 지원하며, 호출할 때마다 새 클라이언트 객체를 생성한다.
	 *
	 * @param <T>	반환할 서비스 객체의 타입.
	 * @param svcClass	검색할 서비스 클래스.
	 * @return	서비스 클라이언트 객체. 지원하지 않는 서비스 클래스인 경우는 {@code null}.
	 */
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
			return null;
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
	
	/**
	 * MDTManager가 사용하는 {@link MDTInstanceManager} proxy 객체를 반환한다.
	 * 
	 * @return	{@link MDTInstanceManager} proxy 객체
	 */
	public HttpMDTInstanceManager getInstanceManager() {
		String endpoint = String.format("%s%s", getEndpoint(), INSTANCE_MANAGER_SUFFIX);
		return HttpMDTInstanceManager.builder()
									.httpClient(getHttpClient())
									.endpoint(endpoint)
									.build();
	}
	
	/**
	 * MDTManager가 사용하는 {@link WorkflowManager} proxy 객체를 반환한다.
	 *
	 * @return {@link WorkflowManager} proxy 객체
	 */
	@Override
	public HttpWorkflowManager getWorkflowManager() {
//		return createClient(WorkflowModelManager.class);
		return new HttpWorkflowManager(getHttpClient(), m_wfMgrEndpoint + WORKFLOW_MANAGER_SUFFIX);
	}
	
	/**
	 * MQTT로 수신된 MDTInstance 관련 이벤트({@link MDTManagerMqttMessage})를 받을 구독자를 등록한다.
	 * <p>
	 * 구독자는 JVM 전역 {@link EventBus}에 등록되므로 모든 {@link HttpMDTManager} 객체가 수신한
	 * 이벤트를 받는다. 이벤트는 MQTT endpoint가 설정된 {@link HttpMDTManager}가 생성된 경우에만 발생한다.
	 *
	 * @param subscriber	{@link com.google.common.eventbus.Subscribe} 메소드를 가진 구독자 객체.
	 */
	public static void register(Object subscriber) {
		EVENT_BUS.register(subscriber);
	}

	/**
	 * MDTManager에 {@code GET <endpoint>/health} 요청을 보내 접속 가능 여부를 확인한다.
	 *
	 * @throws utils.http.RESTfulIOException	서버 접속에 실패한 경우.
	 * @throws utils.http.RESTfulRemoteException	서버가 오류를 응답한 경우.
	 */
	public void ping() {
		// mdt-manager의 MDTManagerController는 '/health'로 응답한다.
		String url = String.format("%s/health", getEndpoint());
		m_restfulClient.get(url);
	}

    // @DeleteMapping({"/shutdown"})
	/**
	 * MDTManager 종료를 요청한다.
	 * <p>
	 * mdt-manager가 원격 종료({@code DELETE <endpoint>/shutdown})를 제공하지 않으므로 지원하지 않는다.
	 * 아무 동작 없이 반환하면 종료된 것으로 오해할 수 있으므로 예외를 던진다.
	 *
	 * @throws UnsupportedOperationException	항상.
	 */
	public void shutdown() {
//		String url = String.format("%s/shutdown", getEndpoint());
//		m_restfulClient.delete(url);
		throw new UnsupportedOperationException("MDTManager does not support remote shutdown");
	}

	/**
	 * {@link HttpMDTManager} 빌더를 생성한다.
	 *
	 * @return	빌더 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link HttpMDTManager} 빌더.
	 */
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private Duration m_connectTimeout;
		private Duration m_readTimeout;
		private String m_mqttEndpoint;
		private JsonMapper m_mapper;
		private String m_wfMgrEndpoint;
		
		private Builder() { }
		
		/**
		 * 설정된 값으로 {@link HttpMDTManager} 객체를 생성한다.
		 * <p>
		 * 모든 인증서를 신뢰하는 OkHttp 클라이언트를 생성하여 사용한다.
		 * MQTT endpoint가 설정된 경우에는 이 시점에 MQTT broker에 접속하여 토픽을 구독한다.
		 * 이 때 접속에 실패하면 경고 로그만 남기고 계속 진행한다.
		 *
		 * @return	생성된 {@link HttpMDTManager} 객체.
		 * @throws IllegalStateException	endpoint가 설정되지 않은 경우.
		 * @throws InternalException	HTTP 클라이언트 생성에 실패한 경우.
		 */
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
				m_mapper = Nullables.orGet(m_mapper, MDTModelSerDe::getJsonMapper);
			}
			catch ( KeyManagementException | NoSuchAlgorithmException e ) {
				throw new InternalException("Failed to create HttpClient", e);
			}
			
			return new HttpMDTManager(this);
		}
		
		/**
		 * MDTManager endpoint를 설정한다.
		 * <p>
		 * 워크플로우 관리자 endpoint가 아직 설정되지 않은 경우에는 이 값으로 함께 설정된다.
		 *
		 * @param endpoint	MDTManager endpoint.
		 * @return	빌더 객체.
		 */
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
		
		/**
		 * HTTP 접속 제한시간을 설정한다.
		 *
		 * @param to	접속 제한시간. {@code null}이면 OkHttp 기본값을 사용한다.
		 * @return	빌더 객체.
		 */
		public Builder connectTimeout(Duration to) {
			m_connectTimeout = to;
			return this;
		}
		
		/**
		 * HTTP 읽기 제한시간을 설정한다.
		 *
		 * @param to	읽기 제한시간. {@code null}이면 OkHttp 기본값을 사용한다.
		 * @return	빌더 객체.
		 */
		public Builder readTimeout(Duration to) {
			m_readTimeout = to;
			return this;
		}
		
		/**
		 * MDTInstance 이벤트를 수신할 MQTT broker endpoint를 설정한다.
		 *
		 * @param endpoint	MQTT broker endpoint (예: {@code tcp://localhost:1883}).
		 * 					{@code null}이면 MQTT 이벤트를 수신하지 않는다.
		 * @return	빌더 객체.
		 */
		public Builder mqttEndpoint(String endpoint) {
			m_mqttEndpoint = endpoint;
			return this;
		}
		
		/**
		 * 서버 오류 응답 해석에 사용할 JSON 매퍼를 설정한다.
		 * <p>
		 * 설정하지 않으면 {@link MDTModelSerDe#getJsonMapper()}를 사용한다.
		 *
		 * @param mapper	JSON 매퍼.
		 * @return	빌더 객체.
		 */
		public Builder jsonMapper(JsonMapper mapper) {
			m_mapper = mapper;
			return this;
		}
		
		/**
		 * 워크플로우 관리자가 동작하는 서버의 endpoint를 설정한다.
		 * <p>
		 * 실제 워크플로우 관리자 endpoint는 이 값에 {@code /workflow-manager}를 덧붙인 것이다.
		 *
		 * @param endpoint	워크플로우 관리자 서버 endpoint.
		 * @return	빌더 객체.
		 */
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
