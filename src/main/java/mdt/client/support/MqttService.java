package mdt.client.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.lang3.time.DurationUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractService;

import utils.InternalException;
import utils.LoggerSettable;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.PeriodicLoopExecution;
import utils.func.FOption;
import utils.func.Unchecked;
import utils.thread.Guard;

import ch.qos.logback.classic.LoggerContext;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttService extends AbstractService implements LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttService.class);
	private static final int DEFAULT_SUBSCRIBE_QOS = 1;

	public interface Subscriber {
		public void onMessage(String topic, MqttMessage msg);
	}
	
	private final MqttClient m_client;
	private final MqttConnectOptions m_options;
	private Duration m_reconnectInterval = Duration.ofSeconds(5);
	private MqttBrokerReconnect m_initConnector;
	private Logger m_logger = null;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final ListMultimap<String, Subscriber> m_subscribers = ArrayListMultimap.create();
	// topic별 broker 구독 QoS. 재연결 시 같은 QoS로 다시 구독하기 위해 보관한다.
	@GuardedBy("m_guard") private final Map<String,Integer> m_topicQos = Maps.newHashMap();
	
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
	
	public Duration getReconnectInterval() {
		return m_reconnectInterval;
	}
	public void setReconnectInterval(Duration interval) {
		Preconditions.checkArgument(interval != null && DurationUtils.isPositive(interval),
									"invalid reconnect interval: " + interval);
		
		m_reconnectInterval = interval;
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
			m_client.publish(topic, message);
		}
		finally {
			m_guard.unlock();
		}
	}

	/**
	 * 메시지를 발행하고, broker의 전달 확인을 최대 {@code timeout}까지 기다린다.
	 * <p>
	 * QoS 1, 2 메시지는 broker의 확인(PUBACK, PUBCOMP)을 기다리고, QoS 0 메시지는 전송 즉시 반환한다.
	 * 확인을 기다리는 동안에는 lock을 잡지 않으므로, 이 서비스를 공유하는 다른 발행·구독을 막지 않는다.
	 * <p>
	 * {@link MqttClient}의 동기 발행은 클라이언트 전체에 설정된 대기 시간 하나만 사용할 수 있으므로,
	 * 서비스를 여러 사용자가 공유하는 경우에는 이 메소드로 발행마다 대기 시간을 지정한다.
	 *
	 * @param topic		발행할 topic.
	 * @param message	발행할 메시지.
	 * @param timeout	전달 확인 최대 대기 시간.
	 * @throws MqttException	발행이 실패한 경우. 제한 시간 안에 확인을 받지 못하면 reason code가
	 * 						{@link MqttException#REASON_CODE_CLIENT_TIMEOUT}이다. 이 경우 메시지는
	 * 						broker에 전달되었을 수도 있다.
	 */
	public void publish(String topic, MqttMessage message, Duration timeout) throws MqttException {
		Preconditions.checkArgument(topic != null, "topic is null");
		Preconditions.checkArgument(message != null, "message is null");
		Preconditions.checkArgument(timeout != null && !timeout.isNegative() && !timeout.isZero(),
									"invalid timeout: " + timeout);

		MqttDeliveryToken token = m_guard.getChecked(() -> m_client.getTopic(topic).publish(message));
		token.waitForCompletion(timeout.toMillis());
	}
	
	public void publish(String topic, String payload) throws MqttException {
		publish(topic, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
	}
	
	/**
	 * 주어진 topic(패턴)을 구독한다.
	 * <p>
	 * 같은 topic을 여러 구독자가 구독할 수 있으며, broker에는 topic별로 한 번만 구독된다.
	 * 이때 broker 구독의 QoS는 지금까지 요청된 QoS 중 가장 큰 값이다.
	 * broker와 연결되어 있지 않거나 broker 구독이 실패하더라도 구독자는 등록된 상태로 유지되며,
	 * 이후 broker와 (재)연결될 때 다시 구독된다.
	 *
	 * @param topic			구독할 topic. MQTT 와일드카드({@code +}, {@code #})를 사용할 수 있다.
	 * @param qos			요청 QoS (0, 1, 2).
	 * @param subscriber	메시지를 전달받을 구독자.
	 */
	public void subscribe(String topic, int qos, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");
		Preconditions.checkArgument(qos >= 0 && qos <= 2, "invalid QoS: " + qos);
		Preconditions.checkArgument(subscriber != null, "subscriber is null");

		m_guard.run(() -> {
			m_subscribers.put(topic, subscriber);
			int effectiveQos = Math.max(qos, m_topicQos.getOrDefault(topic, 0));
			m_topicQos.put(topic, effectiveQos);

			if ( isConnectedInGuard() ) {
				try {
					m_client.subscribe(topic, effectiveQos);
				}
				catch ( MqttException e ) {
					getLogger().error("Failed to subscribe to topic[{}], retry on reconnection: {}",
										topic, e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * 주어진 topic(패턴)을 QoS 1로 구독한다.
	 *
	 * @param topic			구독할 topic.
	 * @param subscriber	메시지를 전달받을 구독자.
	 * @see #subscribe(String, int, Subscriber)
	 */
	public void subscribe(String topic, Subscriber subscriber) {
		subscribe(topic, DEFAULT_SUBSCRIBE_QOS, subscriber);
	}

	/**
	 * 주어진 구독자의 topic 구독을 해제한다.
	 * <p>
	 * 같은 topic을 구독하는 다른 구독자가 남아 있으면 broker 구독은 유지된다.
	 *
	 * @param topic			구독 해제할 topic.
	 * @param subscriber	구독 해제할 구독자.
	 */
	public void unsubscribe(String topic, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");

		m_guard.run(() -> {
			if ( !m_subscribers.remove(topic, subscriber) || m_subscribers.containsKey(topic) ) {
				return;
			}

			// 해당 topic의 마지막 구독자가 해제된 경우에만 broker 구독을 해제한다.
			m_topicQos.remove(topic);
            if ( isConnectedInGuard() ) {
                try {
                    m_client.unsubscribe(topic);
                }
                catch ( MqttException e ) {
                	getLogger().error("Failed to unsubscribe from topic[{}]: {}", topic, e.getMessage(), e);
                }
            }
		});
	}
	
	@Override
	public Logger getLogger() {
		return (m_logger != null) ? m_logger : s_logger;
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
			// MQTT Broker에 연결될 때까지 반복적으로 연결을 시도한다.
			m_initConnector = new MqttBrokerReconnect(m_client, m_options, m_reconnectInterval);
			m_initConnector.start();
			
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
		
		// 첫 connection도 완료되지 않은 상태라면 연결 시도를 중단시키고, 중단될 때까지 기다린다.
		m_initConnector.cancel(true);
		Unchecked.runOrIgnore(m_initConnector::waitForFinished);

		// 중지된 서비스는 다시 시작할 수 없으므로, 연결을 끊은 뒤 MqttClient의 자원도 반납한다.
		m_guard.run(() -> {
			if ( m_client.isConnected() ) {
				try {
					m_client.disconnect();
				}
				catch ( MqttException e ) {
					getLogger().error("Failed to disconnect {}", this, e);
				}
			}
			try {
				m_client.close();
			}
			catch ( MqttException e ) {
				getLogger().warn("Failed to close MqttClient: {}, cause={}", this, ""+e);
			}
		});

		notifyStopped();
	}
	
	private MqttCallbackExtended m_callback = new MqttCallbackExtended() {
		@Override
		public void connectionLost(Throwable cause) {
			getLogger().warn("MQTT broker disconnected: broker={}, cause={}", m_client.getServerURI(), ""+cause);
		}

		@Override
		public void messageArrived(String topic, MqttMessage msg) throws Exception {
			getLogger().debug("Message arrived on topic[{}]: {}", topic, msg);
			dispatch(topic, msg);
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			getLogger().debug("connectComplete: reconnect={}, server={}", reconnect, serverURI);
			onConnected(m_client);
		}
	};
	
	/**
	 * 수신한 메시지를 topic이 일치하는 모든 구독자에게 전달한다.
	 * <p>
	 * 구독 목록은 다른 thread에서 변경될 수 있으므로 lock 안에서 전달 대상을 복사하고,
	 * 구독자 호출은 lock 밖에서 수행한다 (구독자 처리가 다른 구독·발행을 막지 않도록).
	 * 한 구독자의 처리가 실패해도 나머지 구독자에게는 계속 전달한다.
	 *
	 * @param topic	메시지의 topic.
	 * @param msg	수신한 메시지.
	 */
	void dispatch(String topic, MqttMessage msg) {
		List<Subscriber> targets = Lists.newArrayList();
		m_guard.run(() -> {
			for ( Map.Entry<String,Subscriber> ent : m_subscribers.entries() ) {
				if ( matchTopic(ent.getKey(), topic) ) {
					targets.add(ent.getValue());
				}
			}
		});

		for ( Subscriber subscriber: targets ) {
			try {
				subscriber.onMessage(topic, msg);
			}
			catch ( Throwable e ) {
				getLogger().warn("Subscriber failed to handle a message: topic={}, cause={}",
									topic, "" + Throwables.unwrapThrowable(e));
			}
		}
	}

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
	
	private void onConnected(MqttClient client) {
		m_guard.lock();
		try {
			// connection이 생성될 때마다 모든 subscription을 요청된 QoS로 다시 수행한다.
			// 일부 topic의 구독이 실패하더라도 나머지 topic의 구독은 계속 시도한다.
			for ( Map.Entry<String,Integer> ent : m_topicQos.entrySet() ) {
				try {
					client.subscribe(ent.getKey(), ent.getValue());
				}
				catch ( MqttException e ) {
					getLogger().error("Failed to resubscribe to topic[{}]: {}", ent.getKey(), e.getMessage());
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
	
	public static final void main(String... args) throws Exception {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger)root).setLevel(ch.qos.logback.classic.Level.DEBUG);

		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setAutomaticReconnect(true);
		opts.setCleanSession(true);
		
		MqttService mqttSvc = new MqttService("tcp://localhost:1883", opts);
		mqttSvc.setReconnectInterval(Duration.ofSeconds(3));
		mqttSvc.startAsync();
		
		mqttSvc.subscribe("test/topic/+", (topic, msg) -> {
			System.out.println("Received message on topic[" + topic + "]: " + new String(msg.getPayload(), StandardCharsets.UTF_8));
		});
		
		PeriodicLoopExecution<Void> publishing = new PeriodicLoopExecution<Void>(Duration.ofSeconds(1)) {
			@Override
			protected FOption<Void> performPeriodicAction(long loopIndex) {
				try {
					mqttSvc.publish("test/topic/1", "Hello MQTT!");
				}
				catch ( MqttException ignored ) {
					System.out.println("Publish failed, broker not connected.");
				}
				return FOption.empty();
			}
		};
		publishing.start();
		
		Thread.sleep(10_000);
		mqttSvc.stopAsync().awaitTerminated();
		
		Thread.sleep(3_000);
	}
}
