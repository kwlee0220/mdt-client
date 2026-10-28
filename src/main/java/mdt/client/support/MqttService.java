package mdt.client.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.lang3.time.DurationUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
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
import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
 * MQTT broker 연결 하나를 관리하고, 여러 사용자의 토픽 구독·발행을 받아 주는 서비스.
 * <p>
 * 같은 토픽(패턴)을 여러 구독자가 구독할 수 있으며, broker에는 토픽별로 한 번만 구독한다. 수신한 메시지는
 * 토픽 패턴이 일치하는 모든 구독자에게 전달한다. 연결이 끊어지면 Paho 자동 재연결을 사용하고, 다시 연결되면
 * 모든 토픽을 재구독한다.
 * <p>
 * <b>잠금과 broker 통신.</b> 구독 목록은 내부 잠금으로 보호하지만, broker와의 통신(구독·해제·발행)은 이 잠금 밖에서
 * 수행한다. 메시지 전달({@link #dispatch})도 같은 잠금을 쓰므로, 잠금을 잡은 채 broker 응답을 기다리면 수신이 밀릴 때
 * 응답을 읽지 못해 교착될 수 있기 때문이다. 같은 토픽의 구독·해제 요청이 서로 뒤집히지 않도록 broker 작업은 별도 잠금으로
 * 한 번에 하나씩 수행하며, 작업 시점의 구독 목록을 보고 구독할지 해제할지 정한다. broker 구독·해제는
 * {@link #setOperationTimeout(Duration) 작업 제한 시간}까지만 기다린다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttService extends AbstractService implements LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttService.class);
	private static final int DEFAULT_SUBSCRIBE_QOS = 1;
	/** broker 구독·해제 응답을 기다리는 기본 시간. */
	public static final Duration DEFAULT_OPERATION_TIMEOUT = Duration.ofSeconds(10);

	public interface Subscriber {
		public void onMessage(String topic, MqttMessage msg);
	}
	
	private final BrokerClient m_client;
	private final MqttConnectOptions m_options;
	private Duration m_reconnectInterval = Duration.ofSeconds(5);
	private volatile Duration m_operationTimeout = DEFAULT_OPERATION_TIMEOUT;
	private MqttBrokerReconnect m_initConnector;
	private Logger m_logger = null;
	
	// 구독 목록을 보호한다. 이 잠금을 잡은 채 broker 응답을 기다리지 않는다.
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final ListMultimap<String, Subscriber> m_subscribers = ArrayListMultimap.create();
	// topic별 broker 구독 QoS. 재연결 시 같은 QoS로 다시 구독하기 위해 보관한다.
	@GuardedBy("m_guard") private final Map<String,Integer> m_topicQos = Maps.newHashMap();
	// broker 구독·해제를 한 번에 하나씩 수행하게 한다. 메시지 전달 경로는 이 잠금을 쓰지 않는다.
	private final ReentrantLock m_brokerLock = new ReentrantLock();
	// 재연결 후 재구독을 Paho 콜백 쓰레드 밖에서 수행한다.
	private final ExecutorService m_resubscriber = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
																						.setNameFormat("mqtt-resubscribe-%d")
																						.setDaemon(true)
																						.build());
	
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
			m_client = new BrokerClient(brokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
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
		
		if ( config.getReconnectInterval() != null ) {
			setReconnectInterval(UnitUtils.parseDuration(config.getReconnectInterval()));
		}
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
	
	/**
	 * broker 구독·해제 응답을 기다리는 시간을 반환한다.
	 *
	 * @return	작업 제한 시간. 기본값은 {@link #DEFAULT_OPERATION_TIMEOUT}(10초).
	 */
	public Duration getOperationTimeout() {
		return m_operationTimeout;
	}
	
	/**
	 * broker 구독·해제 응답을 기다리는 시간을 설정한다. 제한 시간 안에 응답이 없으면 해당 작업은 실패로 기록되며,
	 * 구독은 다음 재연결 때 다시 시도된다.
	 *
	 * @param timeout	작업 제한 시간 (양수).
	 */
	public void setOperationTimeout(Duration timeout) {
		Preconditions.checkArgument(timeout != null && DurationUtils.isPositive(timeout),
									"invalid operation timeout: " + timeout);
		m_operationTimeout = timeout;
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

		// broker 확인을 기다리는 동안 구독 목록 잠금을 잡지 않는다(메시지 전달과의 교착 방지).
		m_client.publish(topic, message);
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

		MqttDeliveryToken token = m_client.getTopic(topic).publish(message);
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
	 * <p>
	 * topic은 구독 목록에 넣기 전에 검사하므로, 잘못된 topic이면 아무 상태도 바뀌지 않고 예외가 발생한다.
	 * broker와 연결되어 있지 않거나 broker 구독이 실패(제한 시간 초과 포함)하더라도 구독자는 등록된 상태로 유지되며,
	 * 이후 broker와 (재)연결될 때 다시 구독된다.
	 *
	 * @param topic			구독할 topic. MQTT 와일드카드({@code +}, {@code #})를 사용할 수 있다.
	 * @param qos			요청 QoS (0, 1, 2).
	 * @param subscriber	메시지를 전달받을 구독자.
	 * @return	구독 핸들. {@link Subscription#close()}로 이 구독을 해제한다.
	 * @throws IllegalArgumentException	topic이 MQTT topic 필터 규칙에 맞지 않거나 인자가 잘못된 경우.
	 */
	public Subscription subscribe(String topic, int qos, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");
		Preconditions.checkArgument(qos >= 0 && qos <= 2, "invalid QoS: " + qos);
		Preconditions.checkArgument(subscriber != null, "subscriber is null");
		validateTopicFilter(topic);

		m_guard.run(() -> {
			m_subscribers.put(topic, subscriber);
			m_topicQos.merge(topic, qos, Math::max);
		});
		syncBrokerSubscription(topic);
		
		return new Subscription(topic, qos, subscriber);
	}

	/**
	 * 주어진 topic(패턴)을 QoS 1로 구독한다.
	 *
	 * @param topic			구독할 topic.
	 * @param subscriber	메시지를 전달받을 구독자.
	 * @return	구독 핸들.
	 * @see #subscribe(String, int, Subscriber)
	 */
	public Subscription subscribe(String topic, Subscriber subscriber) {
		return subscribe(topic, DEFAULT_SUBSCRIBE_QOS, subscriber);
	}

	/**
	 * 주어진 구독자의 topic 구독을 해제한다.
	 * <p>
	 * 로컬 구독 목록에서는 곧바로 빠지므로 이후 메시지는 이 구독자에게 전달되지 않는다. 같은 topic을 구독하는 다른
	 * 구독자가 남아 있으면 broker 구독은 유지되고, 마지막 구독자였다면 broker 구독도 해제한다.
	 *
	 * @param topic			구독 해제할 topic.
	 * @param subscriber	구독 해제할 구독자.
	 * @return	broker 쪽 처리까지 확인했으면 {@code true}. broker 구독 해제가 실패하거나 제한 시간을 넘기면
	 * 			{@code false}이며, 이 경우에도 로컬 구독 목록에서는 빠진다.
	 */
	public boolean unsubscribe(String topic, Subscriber subscriber) {
		Preconditions.checkArgument(topic != null, "topic is null");

		boolean lastSubscriber = m_guard.get(() -> {
			if ( !m_subscribers.remove(topic, subscriber) || m_subscribers.containsKey(topic) ) {
				return false;
			}
			// 해당 topic의 마지막 구독자가 해제된 경우에만 broker 구독을 해제한다.
			m_topicQos.remove(topic);
			return true;
		});
		return !lastSubscriber || syncBrokerSubscription(topic);
	}
	
	/**
	 * 주어진 topic의 broker 구독 상태를 현재 구독 목록에 맞춘다.
	 * <p>
	 * 구독 목록에 topic이 있으면 그 QoS로 구독하고, 없으면 구독을 해제한다. broker 작업은 한 번에 하나씩 수행하고,
	 * 작업할 때의 구독 목록을 보고 결정하므로 같은 topic의 구독·해제가 겹쳐도 마지막 목록 상태로 수렴한다.
	 * 구독 목록 잠금은 잡지 않은 채 broker 응답을 기다린다.
	 *
	 * @return	broker 처리까지 확인했으면(연결되지 않아 다음 연결 때 처리할 경우 포함) {@code true}.
	 */
	private boolean syncBrokerSubscription(String topic) {
		m_brokerLock.lock();
		try {
			if ( !m_client.isConnected() ) {
				// 연결되면 onConnected()가 구독 목록 전체를 다시 구독한다.
				return true;
			}
			Integer qos = m_guard.get(() -> m_topicQos.get(topic));
			IMqttToken token = (qos != null) ? m_client.subscribeAsync(topic, qos) : m_client.unsubscribeAsync(topic);
			token.waitForCompletion(m_operationTimeout.toMillis());
			return true;
		}
		catch ( MqttException e ) {
			getLogger().error("failed to {} topic[{}] at the broker, retry on reconnection: {}",
								m_guard.get(() -> m_topicQos.containsKey(topic)) ? "subscribe" : "unsubscribe",
								topic, ""+e);
			return false;
		}
		finally {
			m_brokerLock.unlock();
		}
	}
	
	/**
	 * MQTT topic 필터 규칙을 검사한다(빈 문자열, 잘못된 위치의 와일드카드 등).
	 *
	 * @param topic	topic 필터.
	 * @throws IllegalArgumentException	규칙에 맞지 않는 경우.
	 */
	public static void validateTopicFilter(String topic) {
		try {
			MqttTopic.validate(topic, true);
		}
		catch ( IllegalArgumentException e ) {
			throw new IllegalArgumentException("invalid MQTT topic filter: '" + topic + "'", e);
		}
	}
	
	/**
	 * {@link MqttService#subscribe}로 등록한 구독 하나를 가리키는 핸들.
	 * <p>
	 * {@link #close()}는 여러 번 호출해도 한 번만 구독을 해제한다.
	 */
	public final class Subscription implements AutoCloseable {
		private final String m_topic;
		private final int m_qos;
		private final Subscriber m_subscriber;
		private final AtomicBoolean m_closed = new AtomicBoolean(false);
		
		private Subscription(String topic, int qos, Subscriber subscriber) {
			m_topic = topic;
			m_qos = qos;
			m_subscriber = subscriber;
		}
		
		public String getTopic() {
			return m_topic;
		}
		
		public int getQos() {
			return m_qos;
		}
		
		public boolean isClosed() {
			return m_closed.get();
		}
		
		/**
		 * 구독을 해제한다. 처음 호출할 때만 해제하며, 이후 호출은 아무것도 하지 않고 {@code true}를 반환한다.
		 *
		 * @return	broker 쪽 처리까지 확인했으면 {@code true}({@link MqttService#unsubscribe} 참고).
		 */
		public boolean unsubscribe() {
			return !m_closed.compareAndSet(false, true) || MqttService.this.unsubscribe(m_topic, m_subscriber);
		}
		
		@Override
		public void close() {
			unsubscribe();
		}
		
		@Override
		public String toString() {
			return String.format("Subscription[topic=%s, qos=%d%s]", m_topic, m_qos, isClosed() ? ", closed" : "");
		}
	}
	
	// 테스트용: 로컬 구독 목록의 상태를 조회한다.
	int getSubscriberCount(String topic) {
		return m_guard.get(() -> m_subscribers.get(topic).size());
	}
	
	Map<String,Integer> getTopicQos() {
		return m_guard.get(() -> Map.copyOf(m_topicQos));
	}
	
	MqttCallbackExtended getCallback() {
		return m_callback;
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

		m_resubscriber.shutdownNow();

		// 중지된 서비스는 다시 시작할 수 없으므로, 연결을 끊은 뒤 MqttClient의 자원도 반납한다.
		// 연결 종료를 기다리는 동안 구독 목록 잠금을 잡지 않는다(메시지 전달과의 교착 방지).
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

		notifyStopped();
	}
	
	private MqttCallbackExtended m_callback = new MqttCallbackExtended() {
		@Override
		public void connectionLost(Throwable cause) {
			getLogger().warn("MQTT broker disconnected: broker={}, cause={}", m_client.getServerURI(), ""+cause);
		}

		@Override
		public void messageArrived(String topic, MqttMessage msg) throws Exception {
			// payload에는 비밀 값이 있을 수 있으므로 메타데이터만 남긴다.
			if ( getLogger().isDebugEnabled() ) {
				getLogger().debug("Message arrived: topic={}, bytes={}, qos={}, retained={}",
									topic, msg.getPayload().length, msg.getQos(), msg.isRetained());
			}
			dispatch(topic, msg);
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			getLogger().debug("connectComplete: reconnect={}, server={}", reconnect, serverURI);
			onConnected();
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
	static boolean matchTopic(String topicPattern, String actualTopic) {
		// '$'로 시작하는 topic(예: $SYS)은 첫 레벨의 와일드카드와 일치하지 않는다(MQTT 3.1.1 4.7.2).
		if ( actualTopic.startsWith("$") && (topicPattern.startsWith("+") || topicPattern.startsWith("#")) ) {
			return false;
		}
		
		// split()의 limit을 -1로 주어 빈 레벨(예: "a/", "a//b")도 하나의 레벨로 보존한다.
		String[] patternTokens = topicPattern.split("/", -1);
		String[] topicTokens = actualTopic.split("/", -1);
		for ( int i = 0; i < patternTokens.length; ++i ) {
			// '#'는 나머지 모든 레벨과 일치하며, 상위 레벨 자체도 포함한다("a/#"는 "a"와 일치).
			if ( patternTokens[i].equals("#") ) {
				return true;
			}
			if ( i >= topicTokens.length ) {
				return false;
			}
			// '+'는 빈 레벨을 포함해 한 레벨의 어떤 값과도 일치한다.
			if ( !patternTokens[i].equals("+") && !patternTokens[i].equals(topicTokens[i]) ) {
				return false;
			}
		}
		return patternTokens.length == topicTokens.length;
	}
	
	/**
	 * broker와 (재)연결되었을 때 Paho 콜백 쓰레드에서 호출된다.
	 * <p>
	 * 재구독은 broker 응답을 기다려야 하므로 별도 쓰레드에서 수행한다. 콜백 쓰레드에서 기다리면, 재연결 직후 retained
	 * 메시지 등이 몰려 Paho 수신 대기열이 찼을 때 구독 응답을 읽지 못해 교착될 수 있다.
	 */
	private void onConnected() {
		getLogger().info("Connected to {}", this);
		try {
			m_resubscriber.execute(this::resubscribeAll);
		}
		catch ( RejectedExecutionException e ) {
			// 서비스 중지 중에 연결된 경우.
		}
	}
	
	private void resubscribeAll() {
		// connection이 생성될 때마다 모든 subscription을 요청된 QoS로 다시 수행한다.
		// 일부 topic의 구독이 실패하더라도 나머지 topic의 구독은 계속 시도한다.
		for ( String topic: m_guard.get(() -> List.copyOf(m_topicQos.keySet())) ) {
			try {
				syncBrokerSubscription(topic);
			}
			catch ( RuntimeException e ) {
				getLogger().error("Failed to resubscribe to topic[{}]: {}", topic, ""+e);
			}
		}
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
		
		if ( config.getReconnectInterval() != null ) {
			// 연결된 뒤 끊어졌을 때의 Paho 자동 재연결 간격 상한. 첫 연결 재시도 간격은 생성자에서 설정한다.
			Duration interval = UnitUtils.parseDuration(config.getReconnectInterval());
			opts.setMaxReconnectDelay((int)Math.min(interval.toMillis(), Integer.MAX_VALUE));
		}
		
		return opts;
	}
	
	/**
	 * Paho {@link MqttClient}의 비동기 구독·해제를 꺼내 쓰기 위한 하위 클래스.
	 * <p>
	 * {@link MqttClient}의 동기 구독·해제는 클라이언트 전체에 설정한 대기 시간 하나만 쓸 수 있고 기본값은 무한 대기이다.
	 * 작업마다 제한 시간을 두기 위해 내부 비동기 클라이언트의 토큰을 받아 직접 기다린다.
	 */
	static final class BrokerClient extends MqttClient {
		BrokerClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
			super(serverURI, clientId, persistence);
		}
		
		IMqttToken subscribeAsync(String topicFilter, int qos) throws MqttException {
			return aClient.subscribe(topicFilter, qos);
		}
		
		IMqttToken unsubscribeAsync(String topicFilter) throws MqttException {
			return aClient.unsubscribe(topicFilter);
		}
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
