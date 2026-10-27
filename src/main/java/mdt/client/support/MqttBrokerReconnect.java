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
 * A class to connect to an MQTT broker with automatic reconnection.
 * <p>
 * This class attempts to connect to the specified MQTT broker and will retry
 * if the connection fails. The reconnection attempts will be made at a
 * specified interval until a successful connection is established.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerReconnect extends AbstractPeriodicPoller<MqttClient> {
	private static final Logger s_logger = LoggerFactory.getLogger(MqttBrokerReconnect.class);
	private static final Duration DEFAULT_RECONNECT_INTERVAL = Duration.ofSeconds(10);
	
	private final MqttClient m_client;
	private final MqttConnectOptions m_connectOptions;
	private volatile int m_failureCount = 0;	// tryPoll()을 수행하는 thread에서만 변경한다.

	public MqttBrokerReconnect(MqttClient client, MqttConnectOptions opts) {
		super(DEFAULT_RECONNECT_INTERVAL);
		Preconditions.checkNotNull(client);
		Preconditions.checkNotNull(opts);
		
		m_client = client;
		m_connectOptions = opts;
		
		setLogger(s_logger);
	}
	
	public MqttBrokerReconnect(MqttClient client, MqttConnectOptions opts, Duration reconnectInterval) {
		super(reconnectInterval, IntervalMode.FIXED_RATE);
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
