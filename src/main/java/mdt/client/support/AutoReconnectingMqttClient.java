package mdt.client.support;

import java.time.Duration;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;

import utils.LoggerSettable;
import utils.async.Guard;
import utils.func.FOption;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AutoReconnectingMqttClient extends AbstractService
												implements MqttCallback, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AutoReconnectingMqttClient.class);
	
	private final String m_mqttServerUri;
	private final String m_clientId;
	private final Duration m_reconnectInterval;
	private Logger m_logger = s_logger;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private volatile MqttBrokerReconnect m_reconnect;
	@GuardedBy("m_guard") private volatile MqttClient m_mqttClient;
	
	abstract protected void mqttBrokerConnected(MqttClient client) throws Exception;
	abstract protected void mqttBrokerDisconnected();
	
	protected AutoReconnectingMqttClient(String mqttServerUri, String clientId, Duration reconnectInterval) {
		Preconditions.checkNotNull(mqttServerUri);
		Preconditions.checkNotNull(reconnectInterval);
		
		m_mqttServerUri = mqttServerUri;
		m_clientId = FOption.getOrElse(clientId, MqttClient.generateClientId());
		m_reconnectInterval = reconnectInterval;
	}
	
	public MqttClient pollMqttClient() {
		return m_mqttClient;
	}
	
	public MqttClient awaitMqttClient() throws InterruptedException {
		return m_guard.awaitWhileAndGet(() -> m_mqttClient == null, () -> m_mqttClient);
	}

	@Override
	public void connectionLost(Throwable cause) {
		m_guard.runAndSignalAll(() -> m_mqttClient = null);
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("MQTT-Broker disconnected: server={}", m_mqttServerUri);
		}
		
		try {
			mqttBrokerDisconnected();
		}
		catch ( Throwable e ) {
			getLogger().warn("MqttBrokerDisconnection action was failed: cause={}", e);
		}
		
		tryConnect();
	}

	@Override public void messageArrived(String topic, MqttMessage msg) throws Exception { }
	@Override public void deliveryComplete(IMqttDeliveryToken token) { }
	
	@Override
	public Logger getLogger() {
		return m_logger;
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = FOption.getOrElse(logger, s_logger);
	}

	@Override
	protected void doStart() {
		tryConnect();
	}

	@Override
	protected void doStop() { }
	
	private void tryConnect() {
		m_reconnect = MqttBrokerReconnect.builder()
										.mqttServerUri(m_mqttServerUri)
										.clientId(m_clientId)
										.reconnectTryInterval(m_reconnectInterval)
										.build();
		m_reconnect.setLogger(getLogger());
		m_reconnect.whenFinished(result -> {
			if ( result.isSuccessful() ) {
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("MQTT-Broker connected: server={}", m_mqttServerUri);
				}
				m_guard.runAnSignalAllOrThrow(() -> {
					m_mqttClient = result.getUnchecked();
					m_mqttClient.setCallback(this);
					m_reconnect = null;
				});
				
				try {
					mqttBrokerConnected(m_mqttClient);
				}
				catch ( Exception e ) {
					s_logger.error("MqttBrokerConnection action has been failed: cause={}", e);
				}
			}
			else {
				m_guard.runAnSignalAllOrThrow(() -> m_reconnect = null);
			}
		});

		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("trying to connect to MQTT-Broker: server={}", m_mqttServerUri);
		}
		m_reconnect.start();
	}
}
