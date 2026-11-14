package mdt.client;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.jetbrains.annotations.Nullable;

import utils.UnitUtils;
import utils.Utilities;
import utils.func.FOption;
import utils.io.FileUtils;
import utils.io.IOUtils;


/**
 * MDT 클라이언트 설정 ({@code mdt_client_config.yaml}).
 * <p>
 * {@link HttpMDTManager#connect(MDTClientConfig)}가 MDTManager에 접속할 때 사용하는 설정으로,
 * 다음 항목으로 구성된다.
 * <ul>
 * 	<li>{@code mdtUrl}: MDTManager endpoint.
 * 	<li>{@code connectTimeout}, {@code readTimeout}: HTTP 접속/읽기 제한시간 (예: {@code 5s}).
 * 	<li>{@code mqttEndpoint}: MDTInstance 이벤트를 수신할 MQTT broker endpoint.
 * 	<li>{@code workflowManagerUrl}: 워크플로우 관리자 서버 endpoint.
 * </ul>
 * {@code mdtUrl}을 제외한 항목은 생략 가능하며, 생략된 경우 {@code null}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"mdtUrl", "connectTimeout", "readTimeout", "mqttEndpoint", "workflowManagerUrl"})
public final class MDTClientConfig {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTClientConfig.class);

	private String m_mdtUrl;
	private @Nullable Duration m_connectTimeout;
	private @Nullable Duration m_readTimeout;
	private @Nullable String m_mqttEndpoint;
	private @Nullable String m_workflowManagerUrl;
	
	/**
	 * YAML 형식의 설정 파일을 읽어 설정 객체를 생성한다.
	 * <p>
	 * 파일 내용을 해석하기 전에 Apache Commons Text의 {@link StringSubstitutor#createInterpolator()}로
	 * {@code ${env:...}}, {@code ${sys:...}} 등의 변수를 치환한다.
	 *
	 * @param configFile	설정 파일.
	 * @return	설정 객체.
	 * @throws IOException	파일을 읽거나 YAML을 해석하는 데 실패한 경우.
	 */
	public static MDTClientConfig load(File configFile) throws IOException {
		s_logger.debug("reading a configuration from {}", configFile.getAbsolutePath());
		
		String confYamlString = IOUtils.readString(configFile);
		StringSubstitutor interpolator = StringSubstitutor.createInterpolator();
		confYamlString = interpolator.replace(confYamlString);
		
		JsonMapper mapper = JsonMapper.builder(new YAMLFactory()).build();
		MDTClientConfig config = mapper.readValue(confYamlString, MDTClientConfig.class);
		
		return config;
	}
	
	/** 기본 위치에서 찾는 클라이언트 설정 파일 이름. */
	public static final String CONFIG_FILE_NAME = "mdt_client_config.yaml";
	/** {@code MDT_URL} 환경 변수로 설정을 만들 때 쓰는 읽기 제한 시간. */
	private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
	
	/**
	 * 클라이언트 홈 디렉토리를 반환한다.
	 *
	 * @return	환경 변수 {@code MDT_CLIENT_HOME}이 가리키는 디렉토리. 설정되지 않았으면 현재 작업 디렉토리.
	 */
	public static File getClientHomeDir() {
		return Utilities.getEnvironmentVariableFile("MDT_CLIENT_HOME")
						.orElseGet(FileUtils::getCurrentWorkingDirectory);
	}
	
	/**
	 * 기본 위치에서 클라이언트 설정을 찾는다.
	 * <p>
	 * 클라이언트 홈 디렉토리({@link #getClientHomeDir()})의 {@value #CONFIG_FILE_NAME}을 읽을 수 있으면 그 파일을
	 * 읽는다. 파일이 없으면 환경 변수 {@code MDT_URL}로 설정을 만든다(읽기 제한 시간 30초). 파일이 있는데 읽지 못하면
	 * 환경 변수로 넘어가지 않고 예외를 던진다.
	 *
	 * @return	찾은 설정. 파일도 {@code MDT_URL}도 없으면 빈 값.
	 * @throws IOException	설정 파일을 읽거나 해석하지 못한 경우.
	 */
	public static FOption<MDTClientConfig> loadDefault() throws IOException {
		File configFile = FileUtils.path(getClientHomeDir(), CONFIG_FILE_NAME);
		if ( configFile.canRead() ) {
			return FOption.of(load(configFile));
		}
		
		String mdtUrl = System.getenv("MDT_URL");
		if ( mdtUrl == null ) {
			return FOption.empty();
		}
		s_logger.debug("using environment variable 'MDT_URL': {}", mdtUrl);
		MDTClientConfig config = of(mdtUrl);
		config.setReadTimeout(DEFAULT_READ_TIMEOUT);
		return FOption.of(config);
	}
	
	/**
	 * MDTManager endpoint만 설정된 설정 객체를 생성한다.
	 *
	 * @param mdtUrl	MDTManager endpoint.
	 * @return	설정 객체.
	 */
	public static MDTClientConfig of(String mdtUrl) {
		MDTClientConfig config = new MDTClientConfig();
		config.setMdtUrl(mdtUrl);
		
		return config;
	}
	
	/**
	 * MDTManager endpoint를 반환한다.
	 *
	 * @return	MDTManager endpoint.
	 */
	public String getMdtUrl() {
		return m_mdtUrl;
	}
	
	/**
	 * MDTManager endpoint를 설정한다.
	 *
	 * @param url	MDTManager endpoint.
	 */
	public void setMdtUrl(String url) {
		m_mdtUrl = url;
	}
	
	/**
	 * HTTP 접속 제한시간을 반환한다.
	 *
	 * @return	접속 제한시간. 설정되지 않은 경우는 {@code null}.
	 */
	public Duration getConnectTimeout() {
		return m_connectTimeout;
	}
	
	/**
	 * HTTP 접속 제한시간을 설정한다.
	 *
	 * @param timeout	접속 제한시간.
	 */
	public void setConnectTimeout(Duration timeout) {
		m_connectTimeout = timeout;
	}
	
	/**
	 * 문자열 형식의 HTTP 접속 제한시간을 설정한다. YAML의 {@code connectTimeout} 항목에 대응한다.
	 *
	 * @param str	{@link UnitUtils#parseDuration(String)} 형식의 시간 문자열 (예: {@code 5s}).
	 */
	@JsonProperty("connectTimeout")
	public void setConnectTimeoutString(String str) {
		m_connectTimeout = UnitUtils.parseDuration(str);
	}
	
	/**
	 * HTTP 읽기 제한시간을 반환한다.
	 *
	 * @return	읽기 제한시간. 설정되지 않은 경우는 {@code null}.
	 */
	public Duration getReadTimeout() {
		return m_readTimeout;
	}
	
	/**
	 * HTTP 읽기 제한시간을 설정한다.
	 *
	 * @param timeout	읽기 제한시간.
	 */
	public void setReadTimeout(Duration timeout) {
		m_readTimeout = timeout;
	}
	
	/**
	 * 문자열 형식의 HTTP 읽기 제한시간을 설정한다. YAML의 {@code readTimeout} 항목에 대응한다.
	 *
	 * @param str	{@link UnitUtils#parseDuration(String)} 형식의 시간 문자열 (예: {@code 30s}).
	 */
	@JsonProperty("readTimeout")
	public void setReadTimeoutString(String str) {
		this.m_readTimeout = UnitUtils.parseDuration(str);
	}
	
	/**
	 * MQTT broker endpoint를 반환한다.
	 *
	 * @return	MQTT broker endpoint. 설정되지 않은 경우는 {@code null}.
	 */
	public String getMqttEndpoint() {
		return m_mqttEndpoint;
	}
	
	/**
	 * MQTT broker endpoint를 설정한다.
	 *
	 * @param endpoint	MQTT broker endpoint.
	 */
	public void setMqttEndpoint(String endpoint) {
		m_mqttEndpoint = endpoint;
	}
	
	/**
	 * 워크플로우 관리자 서버 endpoint를 반환한다.
	 *
	 * @return	워크플로우 관리자 서버 endpoint. 설정되지 않은 경우는 {@code null}.
	 */
	public String getWorkflowManagerUrl() {
		return m_workflowManagerUrl;
	}
	
	/**
	 * 워크플로우 관리자 서버 endpoint를 설정한다.
	 *
	 * @param endpoint	워크플로우 관리자 서버 endpoint.
	 */
	public void setWorkflowManagerUrl(String endpoint) {
		m_workflowManagerUrl = endpoint;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MDTClientConfig [mdtUrl=").append(m_mdtUrl)
				.append(", connectTimeout=").append(m_connectTimeout)
				.append(", readTimeout=").append(m_readTimeout)
				.append(", mqttEndpoint=").append(m_mqttEndpoint)
				.append(", workflowManagerUrl=").append(m_workflowManagerUrl)
				.append("]");
		return builder.toString();
	}
}
	
