package mdt.client.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import utils.func.Optionals;


/**
 * MQTT broker 연결 설정.
 * <p>
 * JSON 필드는 {@code brokerUrl}, {@code connectionTimeout}, {@code keepAliveInterval}이다.
 * 그 밖의 필드는 사용되지 않으며, 오타로 설정이 무시되는 것을 알 수 있도록 읽을 때 경고 로그를 남긴다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerConfig {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttBrokerConfig.class);
	private static final String DEFAULT_BROKER_URL = "tcp://localhost:1883";

	private String m_brokerUrl;
	private String m_connectionTimeout;	// null means using default value of MQTT library.
	private String m_keepAliveInterval;	// null means using default value of MQTT library.
	
	public String getBrokerUrl() {
		return Optionals.getOrElse(m_brokerUrl, DEFAULT_BROKER_URL);
	}
	
	public void setBrokerUrl(String brokerUrl) {
		m_brokerUrl = brokerUrl;
	}
	
	public String getConnectionTimeout() {
		return m_connectionTimeout;
	}
	
	public void setConnectionTimeout(String connectionTimeout) {
		m_connectionTimeout = connectionTimeout;
	}
	
	public String getKeepAliveInterval() {
		return m_keepAliveInterval;
	}
	
	public void setKeepAliveInterval(String keepAliveInterval) {
		m_keepAliveInterval = keepAliveInterval;
	}

	@JsonAnySetter
	@SuppressWarnings("unused")		// Jackson이 알 수 없는 필드를 읽을 때 호출한다.
	private void warnUnknownField(String name, Object value) {
		s_logger.warn("ignore unknown MQTT broker configuration field: {}={} "
						+ "(supported: brokerUrl, connectionTimeout, keepAliveInterval)", name, value);
	}

	@Override
	public String toString() {
		String connectionTimeout = (m_connectionTimeout != null) ? m_connectionTimeout : "default";
		String keepAliveInterval = (m_keepAliveInterval != null) ? m_keepAliveInterval : "default";
		return String.format("%s: broker=%s, connection-timeout=%s, keep-alive=%s",
								getClass().getSimpleName(), getBrokerUrl(), connectionTimeout, keepAliveInterval);
	}
}
