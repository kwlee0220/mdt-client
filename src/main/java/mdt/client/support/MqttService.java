package mdt.client.support;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.util.concurrent.AbstractService;

import utils.KeyValue;
import utils.Throwables;
import utils.async.Guard;
import utils.func.Unchecked;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttService extends AbstractService {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttService.class);
	private static final MqttConnectOptions MQTT_CONNECT_OPTIONS = new MqttConnectOptions();
	static {
		MQTT_CONNECT_OPTIONS.setCleanSession(true);
		MQTT_CONNECT_OPTIONS.setAutomaticReconnect(true);
	}
	
	public interface Subscriber {
		public void onMessage(String topic, MqttMessage msg);
	}
	
	private final String m_brokerUrl;
	private final String m_clientId;
	
	private final MqttConnectOptions m_options;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private MqttClient m_client;
	@GuardedBy("m_guard") private final ListMultimap<String, Subscriber> m_subscribers = ArrayListMultimap.create();
	
	public MqttService(String brokerUrl, String clientId, MqttConnectOptions opts) {
		m_brokerUrl = brokerUrl;
		m_clientId = clientId;
		m_options = opts;
	}
	public MqttService(String brokerUrl, String clientId) {
		this(brokerUrl, clientId, MQTT_CONNECT_OPTIONS);
	}
	
	public boolean isConnected() {
		return m_guard.get(this::isConnectedInGuard);
	}
	
	private boolean isConnectedInGuard() {
		return m_client != null && m_client.isConnected();
	}
	
	public @Nullable MqttClient getMqttClient() {
		return m_guard.get(() -> isConnectedInGuard() ? m_client : null);
	}
	
	public void publish(String topic, String payload) throws MqttException {
		Preconditions.checkArgument(topic != null, "topic is null");
		Preconditions.checkArgument(payload != null, "payload is null");
		
		m_guard.lock();
		try {
			if ( !isConnectedInGuard() ) {
				s_logger.warn("failed to publish: topic={}, message={}, broker={}, cause=not connected to MQTT broker",
								topic, payload, m_brokerUrl);
				return;
			}
			
			MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
			m_client.publish(topic, message);
		}
		finally {
			m_guard.unlock();
		}
	}
	
	public void subscribe(String topic, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");
		
		m_guard.runChecked(() -> {
			m_subscribers.put(topic, subscriber);
			
			if ( isConnectedInGuard() ) {
				try {
					m_client.subscribe(topic);
				}
				catch ( MqttException e ) {
					s_logger.error("Failed to subscribe to topic[{}]: {}", topic, e.getMessage(), e);
				}
			}
		});
	}
	
	public void unsubscribe(String topic, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");
		
		m_guard.runChecked(() -> {
			if ( m_subscribers.remove(topic, subscriber) ) {
	            if ( isConnectedInGuard() ) {
	                try {
	                    m_client.unsubscribe(topic);
	                }
	                catch ( MqttException e ) {
	                    s_logger.error("Failed to unsubscribe from topic[{}]: {}", topic, e.getMessage(), e);
	                }
	            }
			}
		});
	}
	
	@Override
	public String toString() {
		return "MqttService[broker=" + m_brokerUrl + ", clientId=" + m_clientId + "]";
	}
	
	@Override
	protected void doStart() {
		try {
			MqttClient client = new MqttClient(m_brokerUrl, m_clientId, new MemoryPersistence());

	        // Set callback for message arrival and connection loss
	        client.setCallback(new MqttCallback() {
	            @Override
	            public void connectionLost(Throwable cause) {
        			s_logger.warn("MQTT broker disconnected: broker={}, cause={}", m_brokerUrl, cause);
	            }
	
	            @Override
	            public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    s_logger.debug("Message arrived on topic[{}]: {}", topic, msg);
	        		
	        		var subscriptions
	        			= m_guard.get(() -> FStream.from(m_subscribers.keys())
							        				.filter(topic::startsWith)
							        				.flatMap(k -> FStream.from(m_subscribers.get(k))
							        									.map(sub -> KeyValue.of(k, sub)))
							        				.toList());
	            	for ( KeyValue<String,Subscriber> sub: subscriptions ) {
	            		sub.value().onMessage(topic, msg);
	            	}
	            }
	
	            @Override
	            public void deliveryComplete(IMqttDeliveryToken token) {}
	        });
	
	        // Connect to broker
	        m_options.setCleanSession(true);
	        client.connect(m_options);
	        s_logger.info("Connected to {}", this);
			
			m_guard.run(() -> {
				if ( isConnectedInGuard() ) {
					Unchecked.runOrIgnore(() -> client.disconnect());
					
					s_logger.warn("MQTT broker is already connected");
					throw new IllegalStateException("MQTT broker is already connected");
				}
				
				m_client = client;
			});
	        
	        // subscribe to all topics
			for ( String topic : m_subscribers.keySet() ) {
				client.subscribe(topic);
			}
			
			notifyStarted();
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			notifyFailed(cause);
		}
	}
	
	@Override
	protected void doStop() {
		m_guard.runChecked(() -> {
			if ( m_client != null ) {
				try {
					m_client.disconnect();
				}
				catch ( MqttException e ) {
					s_logger.error("Failed to disconnect {}", this, e);
				}
				
				m_client = null;
			}
		});

		notifyStopped();
	}
}
