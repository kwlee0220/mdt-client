package mdt.client;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Data;
import lombok.NoArgsConstructor;

import utils.UnitUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@Data
public final class MDTClientConfig {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTClientConfig.class);
//	private static final String DEFAULT_ENDPOINT = "http://localhost:12985/instance-manager";

	private String endpoint;
	private Duration connectTimeout;
	private Duration readTimeout;
	private String mqttEndpoint;
	
	public static MDTClientConfig load(File configFile) throws IOException {
		if ( s_logger.isInfoEnabled() ) {
			s_logger.info("reading a configuration from {}", configFile);
		}
		
		JsonMapper mapper = JsonMapper.builder(new YAMLFactory()).build();
		MDTClientConfig config = mapper.readValue(configFile, MDTClientConfig.class);
		
		return config;
	}
	
	@JsonProperty("connectTimeout")
	public void setConnectTimeoutString(String str) {
		this.connectTimeout = UnitUtils.parseDuration(str);
	}
	
	@JsonProperty("readTimeout")
	public void setReadTimeoutString(String str) {
		this.readTimeout = UnitUtils.parseDuration(str);
	}
}
	
