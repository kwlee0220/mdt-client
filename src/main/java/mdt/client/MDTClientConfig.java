package mdt.client;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.commons.text.StringSubstitutor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import utils.UnitUtils;
import utils.io.IOUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"mdtEndpoint", "connectTimeout", "readTimeout", "mqttEndpoint", "workflowManagerEndpoint"})
public final class MDTClientConfig {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTClientConfig.class);

	private String m_mdtEndpoint;
	private @Nullable Duration m_connectTimeout;
	private @Nullable Duration m_readTimeout;
	private @Nullable String m_mqttEndpoint;
	private @Nullable String m_workflowManagerEndpoint;
	
	public static MDTClientConfig load(File configFile) throws IOException {
		if ( s_logger.isInfoEnabled() ) {
			s_logger.info("reading a configuration from {}", configFile);
		}
		
		String confYamlString = IOUtils.toString(configFile);
		StringSubstitutor interpolator = StringSubstitutor.createInterpolator();
		confYamlString = interpolator.replace(confYamlString);
		
		JsonMapper mapper = JsonMapper.builder(new YAMLFactory()).build();
		MDTClientConfig config = mapper.readValue(confYamlString, MDTClientConfig.class);
		
		return config;
	}
	
	public static MDTClientConfig of(String mdtEndpoint) {
		MDTClientConfig config = new MDTClientConfig();
		config.setMdtEndpoint(mdtEndpoint);
		
		return config;
	}
	
	public String getMdtEndpoint() {
		return m_mdtEndpoint;
	}
	
	public void setMdtEndpoint(String endpoint) {
		m_mdtEndpoint = endpoint;
	}
	
	public Duration getConnectTimeout() {
		return m_connectTimeout;
	}
	
	public void setConnectTimeout(Duration timeout) {
		m_connectTimeout = timeout;
	}
	
	@JsonProperty("connectTimeout")
	public void setConnectTimeoutString(String str) {
		m_connectTimeout = UnitUtils.parseDuration(str);
	}
	
	public Duration getReadTimeout() {
		return m_readTimeout;
	}
	
	public void setReadTimeout(Duration timeout) {
		m_readTimeout = timeout;
	}
	
	@JsonProperty("readTimeout")
	public void setReadTimeoutString(String str) {
		this.m_readTimeout = UnitUtils.parseDuration(str);
	}
	
	public String getMqttEndpoint() {
		return m_mqttEndpoint;
	}
	
	public void setMqttEndpoint(String endpoint) {
		m_mqttEndpoint = endpoint;
	}
	
	public String getWorkflowManagerEndpoint() {
		return m_workflowManagerEndpoint;
	}
	
	public void setWorkflowManagerEndpoint(String endpoint) {
		m_workflowManagerEndpoint = endpoint;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MDTClientConfig [mdtEndpoint=").append(m_mdtEndpoint)
				.append(", connectTimeout=").append(m_connectTimeout)
				.append(", readTimeout=").append(m_readTimeout)
				.append(", mqttEndpoint=").append(m_mqttEndpoint)
				.append(", workflowManagerEndpoint=").append(m_workflowManagerEndpoint)
				.append("]");
		return builder.toString();
	}
}
	
