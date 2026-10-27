package mdt.client.support;

import java.time.Duration;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;


/**
 * 접속할 수 없는 broker에 대한 {@link MqttBrokerReconnect}의 실패 로그를 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerReconnectTest {
	private static final String UNREACHABLE_BROKER = "tcp://127.0.0.1:1";

	private Logger m_logger;
	private ListAppender<ILoggingEvent> m_appender;

	@BeforeEach
	public void setUp() {
		m_logger = (Logger)LoggerFactory.getLogger(MqttBrokerReconnect.class);
		m_appender = new ListAppender<>();
		m_appender.start();
		m_logger.addAppender(m_appender);
	}

	@AfterEach
	public void tearDown() {
		m_logger.detachAppender(m_appender);
	}

	@Test
	public void testWarnOnlyOnFirstFailure() throws Exception {
		MqttClient client = new MqttClient(UNREACHABLE_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
		MqttBrokerReconnect reconnect = new MqttBrokerReconnect(client, new MqttConnectOptions(),
																Duration.ofMillis(100));
		reconnect.start();
		try {
			long due = System.currentTimeMillis() + 5_000;
			while ( reconnect.getFailureCount() < 3 && System.currentTimeMillis() < due ) {
				Thread.sleep(50);
			}
			Assertions.assertTrue(reconnect.getFailureCount() >= 3, "failures=" + reconnect.getFailureCount());
		}
		finally {
			reconnect.cancel(true);
			reconnect.waitForFinished();
			client.close();
		}

		// 실패가 여러 번 반복되어도 경고는 한 번만 남는다.
		List<ILoggingEvent> warnings = m_appender.list.stream()
												.filter(ev -> ev.getLevel() == Level.WARN)
												.toList();
		Assertions.assertEquals(1, warnings.size(), "" + warnings);
		Assertions.assertTrue(warnings.get(0).getFormattedMessage().contains(UNREACHABLE_BROKER),
								warnings.get(0).getFormattedMessage());
	}
}
