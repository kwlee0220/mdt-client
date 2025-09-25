package mdt.client.support;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.func.FOption;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonIncludeProperties({ "brokerUrl", "connectionTimeout", "keepAliveInterval" })
public class MqttBrokerConfig {
	private static final String DEFAULT_BROKER_URL = "tcp://localhost:1883";
	public static final MqttBrokerConfig DEFAULT = new MqttBrokerConfig(null, null, null);
	
	private final String m_brokerUrl;
	private final String m_connectionTimeout;	// null means using default value of MQTT library.
	private final String m_keepAliveInterval;	// null means using default value of MQTT library.
	
	public MqttBrokerConfig(@Nullable @JsonProperty("brokerUrl") String brokerUrl,
							@Nullable @JsonProperty("connectTimeout") String connectionTimeout,
							@Nullable @JsonProperty("keepAliveInterval") String keepAliveInterval) {
		m_brokerUrl = FOption.getOrElse(brokerUrl, DEFAULT_BROKER_URL);
		m_connectionTimeout = connectionTimeout;
		m_keepAliveInterval = keepAliveInterval;
	}
	
	public String getBrokerUrl() {
		return m_brokerUrl;
	}
	
	public String getConnectionTimeout() {
		return m_connectionTimeout;
	}
	
	public String getKeepAliveInterval() {
		return m_keepAliveInterval;
	}
	
	@Override
	public String toString() {
		String connectionTimeout = (m_connectionTimeout != null) ? m_connectionTimeout : "default";
		String keepAliveInterval = (m_keepAliveInterval != null) ? m_keepAliveInterval : "default";
		return String.format("%s: broker=%s, connection-timeout=%s, keep-alive=%s",
								getClass().getSimpleName(), getBrokerUrl(), connectionTimeout, keepAliveInterval);
	}
}
