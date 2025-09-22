package mdt.client.support;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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
	
	private final String m_mqttServerUri;
	private final String m_clientId;
	private final Duration m_reconnectInterval;
	private final @Nullable MqttCallback m_mqttCallback;
	
	@Override protected void initializeLoop() throws Exception { }
	@Override protected void finalizeLoop() throws Exception { }
	
	private MqttBrokerReconnect(Builder builder) {
		Preconditions.checkNotNull(builder.m_mqttServerUri);
		Preconditions.checkNotNull(builder.m_reconnectInterval);
		
		m_mqttServerUri = builder.m_mqttServerUri;
		m_clientId = FOption.getOrElse(builder.m_clientId, MqttClient.generateClientId());
		m_mqttCallback = builder.m_mqttCallback;
		m_reconnectInterval = builder.m_reconnectInterval;
		
		setLogger(s_logger);
	}

	@Override
	protected FOption<MqttClient> iterate(long loopIndex) throws Exception {
		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("retrying {}-th connection to {}", loopIndex+1, m_mqttServerUri);
		}
		
		Instant started = Instant.now();
		try {
			// MQTT Broker에 연결을 시도한다.
			MqttClient mqttClient = connectToMqttBroker();
			
			// MQTT Broker에 연결된 경우 {@link MqttClient} 객체를 반환하고 loop를 종료시킨다
			return FOption.of(mqttClient);
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
	
	private MqttClient connectToMqttBroker() throws MqttException {
		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setCleanSession(true);
		
		MqttClient mqttClient = new MqttClient(m_mqttServerUri, m_clientId, new MemoryPersistence());
		FOption.ifPresent(m_mqttCallback, mqttClient::setCallback);
		mqttClient.connect(opts);
		
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("connected to {}", m_mqttServerUri);
		}
		
		return mqttClient;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_mqttServerUri;
		private String m_clientId;
		private Duration m_reconnectInterval = Duration.ofSeconds(10);
		private @Nullable MqttCallback m_mqttCallback;
		
		public MqttBrokerReconnect build() {
			return new MqttBrokerReconnect(this);
		}
		
		/**
		 * MQTT Broker의 URI를 설정한다.
		 *
		 * @param uri MQTT Broker URI
		 * @return 본 객체.
		 */
		public Builder mqttServerUri(String uri) {
			m_mqttServerUri = uri;
			return this;
		}
		
		/**
		 * MQTT Broker에 연결하기 위한 Client ID를 설정한다.
		 * <p>
		 * 별도로 설정하지 않으면 자동으로 생성된 Client ID가 사용된다.
		 *
		 * @param id Client ID
		 * @return 본 객체.
		 */
		public Builder clientId(String id) {
			m_clientId = id;
			return this;
		}
		
		/**
		 * MQTT connection에 사용할 callback 객체를 설정한다.
		 *
		 * @param callback	callback 객체
		 * @return	본 객체.
		 */
		public Builder mqttCallback(MqttCallback callback) {
			m_mqttCallback = callback;
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
