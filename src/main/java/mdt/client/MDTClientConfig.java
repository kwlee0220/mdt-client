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

import javax.annotation.Nullable;

import utils.UnitUtils;
import utils.io.IOUtils;


/**
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
	
	public static MDTClientConfig load(File configFile) throws IOException {
		if ( s_logger.isInfoEnabled() ) {
			s_logger.info("reading a configuration from {}", configFile.getAbsolutePath());
		}
		
		String confYamlString = IOUtils.toString(configFile);
		StringSubstitutor interpolator = StringSubstitutor.createInterpolator();
		confYamlString = interpolator.replace(confYamlString);
		
		JsonMapper mapper = JsonMapper.builder(new YAMLFactory()).build();
		MDTClientConfig config = mapper.readValue(confYamlString, MDTClientConfig.class);
		
		return config;
	}
	
	public static MDTClientConfig of(String mdtUrl) {
		MDTClientConfig config = new MDTClientConfig();
		config.setMdtUrl(mdtUrl);
		
		return config;
	}
	
	public String getMdtUrl() {
		return m_mdtUrl;
	}
	
	public void setMdtUrl(String url) {
		m_mdtUrl = url;
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
	
	public String getWorkflowManagerUrl() {
		return m_workflowManagerUrl;
	}
	
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
	
