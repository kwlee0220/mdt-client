package mdt.client.support;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.func.Optionals;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix="m_")
@JsonIncludeProperties({ "brokerUrl", "connectionTimeout", "keepAliveInterval" })
public class MqttBrokerConfig {
	private static final String DEFAULT_BROKER_URL = "tcp://localhost:1883";
	
	private String m_brokerUrl;
	private String m_connectionTimeout;	// null means using default value of MQTT library.
	private String m_keepAliveInterval;	// null means using default value of MQTT library.
	
	public String getBrokerUrl() {
		return Optionals.getOrElse(m_brokerUrl, DEFAULT_BROKER_URL);
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
