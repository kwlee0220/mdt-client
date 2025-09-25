package mdt.client.support;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

import utils.InternalException;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.Guard;
import utils.func.FOption;
import utils.func.Unchecked;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttService extends AbstractService implements LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttService.class);
	
	public interface Subscriber {
		public void onMessage(String topic, MqttMessage msg);
	}
	
	private final MqttClient m_client;
	private final MqttConnectOptions m_options;
	private Logger m_logger = null;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final ListMultimap<String, Subscriber> m_subscribers = ArrayListMultimap.create();
	
	/**
	 * MQTT Service를 생성한다.
	 * 
	 * @param brokerUrl		MQTT Broker URL
	 * @param opts			MQTT 연결 옵션
	 */
	public MqttService(String brokerUrl, MqttConnectOptions opts) {
		Preconditions.checkArgument(brokerUrl != null, "brokerUrl is null");
		Preconditions.checkArgument(opts != null, "MqttConnectOptions is null");
		
		try {
			m_client = new MqttClient(brokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
			m_client.setCallback(m_callback);
			m_options = opts;
		}
		catch ( MqttException e ) {
			throw new InternalException("failed to create MqttClient: broker=" + brokerUrl, e);
		}
	}

	/**
	 * MQTT Service를 생성한다.
	 * 
	 * @param brokerUrl		MQTT Broker URL
	 */
	public MqttService(String brokerUrl) {
		this(brokerUrl, toOptions());
	}
	
	/**
	 * MQTT Service를 생성한다.
	 * 
	 * @param config		MQTT Broker 연결 설정
	 */
	public MqttService(MqttBrokerConfig config) {
		this(config.getBrokerUrl(), toOptions(config));
	}
	
	public MqttConnectOptions getConnectOptions() {
		return m_options;
	}
	
	public boolean isConnected() {
		return m_guard.get(this::isConnectedInGuard);
	}
	
	private boolean isConnectedInGuard() {
		return m_client.isConnected();
	}
	
	public @Nullable MqttClient getMqttClient() {
		return m_guard.get(() -> isConnectedInGuard() ? m_client : null);
	}
	
	public void publish(String topic, MqttMessage message) throws MqttException {
		Preconditions.checkArgument(topic != null, "topic is null");
		Preconditions.checkArgument(message != null, "message is null");
		
		m_guard.lock();
		try {
			if ( !isConnectedInGuard() ) {
				getLogger().warn("failed to publish: topic={}, broker={}, cause=not connected to MQTT broker",
								topic, m_client.getServerURI());
				return;
			}
			
			m_client.publish(topic, message);
		}
		finally {
			m_guard.unlock();
		}
	}
	
	public void publish(String topic, String payload) throws MqttException {
		publish(topic, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
	}
	
	public void subscribe(String topic, int qos, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");
		
		m_guard.runChecked(() -> {
			m_subscribers.put(topic, subscriber);
			
			if ( isConnectedInGuard() ) {
				try {
					m_client.subscribe(topic, qos);
				}
				catch ( MqttException e ) {
					getLogger().error("Failed to subscribe to topic[{}]: {}", topic, e.getMessage(), e);
				}
			}
		});
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
					getLogger().error("Failed to subscribe to topic[{}]: {}", topic, e.getMessage(), e);
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
	                	getLogger().error("Failed to unsubscribe from topic[{}]: {}", topic, e.getMessage(), e);
	                }
	            }
			}
		});
	}
	
	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}
	
	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	@Override
	public String toString() {
		return "MqttService[broker=" + m_client.getServerURI() + "]";
	}
	
	@Override
	protected void doStart() {
		try {
			connectAsync();
			
			notifyStarted();
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			notifyFailed(cause);
		}
	}
	
	@Override
	protected void doStop() {
		getLogger().info("Stopping {}", this);
		
		m_guard.runChecked(() -> {
			if ( m_client.isConnected() ) {
				try {
					m_client.disconnect();
				}
				catch ( MqttException e ) {
					getLogger().error("Failed to disconnect {}", this, e);
				}
			}
		});

		notifyStopped();
	}
	
	private MqttCallback m_callback = new MqttCallback() {
		@Override
		public void connectionLost(Throwable cause) {
			getLogger().warn("MQTT broker disconnected: broker={}, cause={}", m_client.getServerURI(), ""+cause);
		}

		@Override
		public void messageArrived(String topic, MqttMessage msg) throws Exception {
			getLogger().debug("Message arrived on topic[{}]: {}", topic, msg);

			for ( Map.Entry<String,Subscriber> ent : m_subscribers.entries() ) {
				if ( matchTopic(ent.getKey(), topic) ) {
					Unchecked.runOrIgnore(() -> ent.getValue().onMessage(topic, msg));
				}
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {}
	};
	
	/**
	 * MQTT 토픽 패턴과 실제 토픽의 일치 여부를 확인한다.
	 * MQTT 토픽 패턴 규칙:
	 * - '+': 단일 레벨 와일드카드 (한 레벨의 어떤 값이든 매칭)
	 * - '#': 다중 레벨 와일드카드 (여러 레벨을 포함하는 모든 하위 토픽 매칭)
	 * 
	 * @param topicPattern 구독 패턴 (와일드카드 포함 가능)
	 * @param actualTopic 실제 메시지 토픽
	 * @return 패턴과 토픽이 매칭되면 true, 아니면 false
	 */
	private boolean matchTopic(String topicPattern, String actualTopic) {
	    // 패턴과 토픽이 정확히 일치하는 경우
	    if (topicPattern.equals(actualTopic)) {
	        return true;
	    }
	    
	    // 각 토픽 레벨로 분리
	    String[] patternTokens = topicPattern.split("/");
	    String[] topicTokens = actualTopic.split("/");
	    
	    int patternLength = patternTokens.length;
	    int topicLength = topicTokens.length;
	    int i = 0;
	    
	    for (; i < patternLength && i < topicLength; i++) {
	        // '#'는 다중 레벨 와일드카드로, 이후의 모든 레벨이 매칭됨
	        if (patternTokens[i].equals("#")) {
	            return true;
	        }
	        
	        // '+'는 단일 레벨 와일드카드로, 해당 레벨의 어떤 값이든 매칭됨
	        if (!patternTokens[i].equals("+") && !patternTokens[i].equals(topicTokens[i])) {
	            return false;
	        }
	    }
	    
	    // '#'로 끝나는 패턴의 경우 추가 처리
	    if (i == patternLength - 1 && patternTokens[patternLength - 1].equals("#")) {
	        return true;
	    }
	    
	    // 패턴과 토픽의 레벨 수가 정확히 일치해야 함
	    return i == patternLength && i == topicLength;
	}
	
	private MqttBrokerReconnect connectAsync() {
		// MQTT Broker에 연결될 때까지 반복적으로 연결을 시도한다.
		MqttBrokerReconnect reconnect = MqttBrokerReconnect.builder()
															.mqttClient(m_client)
															.connectOptions(m_options)
															.build();
		reconnect.whenCompleted(client -> {
			onConnected(client);
		});
		reconnect.start();
		
		return reconnect;
	}
	
	private void onConnected(MqttClient client) {
		m_guard.lock();
		try {
			// connection이 생성될 때마나 모든 subscription을 다시 수행한다.
			for ( String topic : m_subscribers.keySet() ) {
				try {
					client.subscribe(topic);
				}
				catch ( MqttException e ) {
					break;
				}
			}
		}
		finally {
			m_guard.unlock();
		}
		getLogger().info("Connected to {}", this);
	}
	
	private static MqttConnectOptions toOptions() {
		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setCleanSession(true);
		opts.setAutomaticReconnect(true);
		
		return opts;
	}
	
	private static MqttConnectOptions toOptions(MqttBrokerConfig config) {
		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setCleanSession(true);
		opts.setAutomaticReconnect(true);
		
		if ( config.getConnectionTimeout() != null ) {
			int connTimeout = (int)UnitUtils.parseDuration(config.getConnectionTimeout()).toSeconds();
			opts.setConnectionTimeout(connTimeout);
		}
		
		if ( config.getKeepAliveInterval() != null ) {
			int keepAlive = (int)UnitUtils.parseDuration(config.getKeepAliveInterval()).toSeconds();
			opts.setKeepAliveInterval(keepAlive);
		}
		
		return opts;
	}
}
