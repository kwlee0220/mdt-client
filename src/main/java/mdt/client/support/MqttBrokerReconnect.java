package mdt.client.support;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.async.AbstractLoopExecution;
import utils.func.FOption;

/**
 * A class to connect to an MQTT broker with automatic reconnection.
 * <p>
 * This class attempts to connect to the specified MQTT broker and will retry
 * if the connection fails. The reconnection attempts will be made at a
 * specified interval until a successful connection is established.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerReconnect extends AbstractLoopExecution<MqttClient> {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttBrokerReconnect.class);
	private static final Duration DEFAULT_RECONNECT_INTERVAL = Duration.ofSeconds(10);
	
	private final MqttClient m_client;
	private final MqttConnectOptions m_connectOptions;
	private final Duration m_reconnectInterval;
	
	@Override protected void initializeLoop() throws Exception { }
	@Override protected void finalizeLoop() throws Exception { }
	
	private MqttBrokerReconnect(Builder builder) {
		Preconditions.checkNotNull(builder.m_client);
		Preconditions.checkNotNull(builder.m_connectOptions);
		
		m_client = builder.m_client;
		m_connectOptions = builder.m_connectOptions;
		m_reconnectInterval = FOption.getOrElse(builder.m_reconnectInterval, DEFAULT_RECONNECT_INTERVAL);
		
		setLogger(s_logger);
	}

	@Override
	protected FOption<MqttClient> iterate(long loopIndex) throws Exception {
		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("trying {}-th connection to {}", loopIndex+1, m_client.getServerURI());
		}
		
		Instant started = Instant.now();
		try {
			// MQTT Broker에 연결을 시도한다.
			m_client.connect(m_connectOptions);
			
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("connected to {}", m_client.getServerURI());
			}
			
			// MQTT Broker에 연결된 경우 {@link MqttClient} 객체를 반환하고 loop를 종료시킨다
			return FOption.of(m_client);
		}
		catch ( MqttException e ) {
			Duration elapsed = Duration.between(started, Instant.now());
			long remains = m_reconnectInterval.minus(elapsed).toMillis();
			if ( remains > 10 ) {
				TimeUnit.MILLISECONDS.sleep(remains);
			}
			
			// MQTT Broker에 연결되지 않은 경우 {@link FOption#empty()}를 반환하여
			// loop를 계속 수행하도록 한다.
			return FOption.empty();
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private MqttClient m_client;
		private MqttConnectOptions m_connectOptions;
		private Duration m_reconnectInterval;
		
		public MqttBrokerReconnect build() {
			return new MqttBrokerReconnect(this);
		}
		
		public Builder mqttClient(MqttClient client) {
			m_client = client;
			return this;
		}
		
		public Builder connectOptions(MqttConnectOptions options) {
			m_connectOptions = options;
			return this;
		}
		
		/**
		 * MQTT Broker에 재접속을 시도하는 간격을 설정한다.
		 *
		 * @param interval 재접속 시도 간격
		 * @return 본 객체.
		 */
		public Builder reconnectInterval(Duration interval) {
			m_reconnectInterval = interval;
			return this;
		}
	}
}
