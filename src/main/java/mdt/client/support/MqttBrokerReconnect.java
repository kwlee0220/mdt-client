package mdt.client.support;

import java.time.Duration;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.async.AbstractPeriodicPoller;
import utils.async.IntervalMode;
import utils.func.FOption;

import ch.qos.logback.classic.LoggerContext;


/**
 * MQTT broker에 연결될 때까지 주기적으로 연결을 시도하는 poller.
 * <p>
 * {@link AbstractPeriodicPoller}로서 주어진 간격마다 {@link MqttClient#connect(MqttConnectOptions)}를
 * 시도하고, 연결에 성공하면 연결된 {@link MqttClient}를 결과로 하여 종료된다.
 * 연결 실패는 첫 번째만 경고 로그로, 이후는 debug 로그로 남긴다.
 * 연결된 이후 끊어졌을 때의 재연결은 이 클래스가 아니라 Paho 자동 재연결 설정이 담당한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerReconnect extends AbstractPeriodicPoller<MqttClient> {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttBrokerReconnect.class);
	private static final Duration DEFAULT_RECONNECT_INTERVAL = Duration.ofSeconds(10);
	
	private final MqttClient m_client;
	private final MqttConnectOptions m_connectOptions;
	private volatile int m_failureCount = 0;	// tryPoll()을 수행하는 thread에서만 변경한다.

	/**
	 * 기본 간격(10초)으로 연결을 시도하는 poller를 생성한다.
	 * <p>
	 * 시도 간격은 {@link IntervalMode#FIXED_DELAY}로 적용된다 (이전 시도가 끝난 시각부터 간격만큼 대기).
	 *
	 * @param client	연결할 MQTT 클라이언트.
	 * @param opts	연결 옵션.
	 * @throws NullPointerException	인자가 {@code null}인 경우.
	 */
	public MqttBrokerReconnect(MqttClient client, MqttConnectOptions opts) {
		super(DEFAULT_RECONNECT_INTERVAL);
		Preconditions.checkNotNull(client);
		Preconditions.checkNotNull(opts);
		
		m_client = client;
		m_connectOptions = opts;
		
		setLogger(s_logger);
	}
	
	/**
	 * 주어진 간격으로 연결을 시도하는 poller를 생성한다.
	 * <p>
	 * 시도 간격은 {@link IntervalMode#FIXED_DELAY}로 적용된다 (이전 시도가 끝난 시각부터 간격만큼 대기).
	 *
	 * @param client	연결할 MQTT 클라이언트.
	 * @param opts	연결 옵션.
	 * @param reconnectInterval	연결 시도 간격.
	 * @throws IllegalArgumentException	{@code client}가 {@code null}인 경우.
	 * @throws NullPointerException	{@code opts}가 {@code null}인 경우.
	 */
	public MqttBrokerReconnect(MqttClient client, MqttConnectOptions opts, Duration reconnectInterval) {
		super(reconnectInterval, IntervalMode.FIXED_DELAY);
		Preconditions.checkArgument(client != null, "MqttClient is null");
		Preconditions.checkNotNull(opts);
		
		m_client = client;
		m_connectOptions = opts;
		
		setLogger(s_logger);
	}
	
	@Override
	protected void initializePoller() throws Exception {
		getLogger().info("starting MQTT broker connection to {} with interval={}",
						m_client.getServerURI(), getLoopInterval());
	}

	@Override
	protected FOption<MqttClient> tryPoll() {
		getLogger().debug("trying connection to {}", m_client.getServerURI());

		try {
			// MQTT Broker에 연결을 시도한다.
			m_client.connect(m_connectOptions);
			if ( m_failureCount > 0 ) {
				getLogger().info("connected to {} after {} failed attempts", m_client.getServerURI(), m_failureCount);
			}
			else {
				getLogger().info("connected to {}", m_client.getServerURI());
			}

			// MQTT Broker에 연결된 경우 {@link MqttClient} 객체를 반환하고 loop를 종료시킨다
			return FOption.of(m_client);
		}
		catch ( MqttException e ) {
			// 연결되지 않는 동안 시도마다 경고가 쌓이지 않도록, 첫 실패만 경고하고 이후는 debug로 남긴다.
			if ( ++m_failureCount == 1 ) {
				getLogger().warn("failed to connect to {}, keep retrying every {}: cause={}",
								m_client.getServerURI(), getLoopInterval(), "" + e);
			}
			else {
				getLogger().debug("failed to connect to {} (attempts={}): cause={}",
								m_client.getServerURI(), m_failureCount, "" + e);
			}
			return FOption.empty();
		}
	}

	/**
	 * 지금까지 실패한 연결 시도 횟수를 반환한다.
	 *
	 * @return	실패한 연결 시도 횟수.
	 */
	public int getFailureCount() {
		return m_failureCount;
	}
	
	/**
	 * {@code tcp://localhost:1883} broker에 1초 간격으로 연결을 시도하는 예제 프로그램.
	 *
	 * @param args	사용하지 않는다.
	 * @throws Exception	예제 실행 중 오류가 발생한 경우.
	 */
	public static final void main(String... args) throws Exception {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger)root).setLevel(ch.qos.logback.classic.Level.DEBUG);
		
		MqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setAutomaticReconnect(true);
		opts.setCleanSession(true);
		
		MqttBrokerReconnect reconnect = new MqttBrokerReconnect(client, opts,
																Duration.ofSeconds(1));
		MqttClient connectedClient = reconnect.run();
		System.out.println("Connected to broker: " + connectedClient.getServerURI());
	}
}
