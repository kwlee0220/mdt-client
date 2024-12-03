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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerReconnect extends AbstractLoopExecution<MqttClient> {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttBrokerReconnect.class);
	
	private final String m_mqttServerUri;
	private final String m_clientId;
	private final Duration m_reconnectTryInterval;
	@Nullable private final MqttCallback m_mqttCallback;
	
	@Override protected void initializeLoop() throws Exception { }
	@Override protected void finalizeLoop() throws Exception { }
	
	public MqttBrokerReconnect(Builder builder) {
		Preconditions.checkNotNull(builder.m_mqttServerUri);
		Preconditions.checkNotNull(builder.m_reconnectTryInterval);
		
		m_mqttServerUri = builder.m_mqttServerUri;
		m_clientId = FOption.getOrElse(builder.m_clientId, MqttClient.generateClientId());
		m_mqttCallback = builder.m_mqttCallback;
		m_reconnectTryInterval = builder.m_reconnectTryInterval;
		
		setLogger(s_logger);
	}

	@Override
	protected FOption<MqttClient> iterate(long loopIndex) throws Exception {
		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("retrying {}-th connection to {}", loopIndex+1, m_mqttServerUri);
		}
		
		Instant started = Instant.now();
		try {
			return FOption.of(connectToMqttBroker());
		}
		catch ( MqttException e ) {
			Duration elapsed = Duration.between(started, Instant.now());
			long remains = m_reconnectTryInterval.minus(elapsed).toMillis();
			if ( remains > 10 ) {
				TimeUnit.MILLISECONDS.sleep(remains);
			}
			
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
		private Duration m_reconnectTryInterval = Duration.ofSeconds(10);
		@Nullable private MqttCallback m_mqttCallback;
		
		public MqttBrokerReconnect build() {
			return new MqttBrokerReconnect(this);
		}
		
		public Builder mqttServerUri(String uri) {
			m_mqttServerUri = uri;
			return this;
		}
		
		public Builder clientId(String id) {
			m_clientId = id;
			return this;
		}
		
		public Builder mqttCallback(MqttCallback callback) {
			m_mqttCallback = callback;
			return this;
		}
		
		public Builder reconnectTryInterval(Duration interval) {
			m_reconnectTryInterval = interval;
			return this;
		}
	}
}
