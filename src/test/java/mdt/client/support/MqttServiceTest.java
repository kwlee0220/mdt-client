package mdt.client.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.Service.State;


/**
 * {@link MqttService}의 구독 관리와 메시지 분배 단위 테스트.
 * <p>
 * broker 없이 검증하기 위해, Paho callback이 호출하는 package-private {@code dispatch()}를 직접 호출한다.
 * broker 측 구독·구독 해제 요청은 연결된 상태에서만 수행되므로 이 테스트에서는 검증하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttServiceTest {
	private static final String UNREACHABLE_BROKER = "tcp://127.0.0.1:1";

	@Test
	public void testDispatchToMatchingSubscribers() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		List<String> received = new CopyOnWriteArrayList<>();

		mqtt.subscribe("a/b", 1, (t, m) -> received.add("exact"));
		mqtt.subscribe("a/+", 0, (t, m) -> received.add("plus"));
		mqtt.subscribe("a/#", (t, m) -> received.add("hash"));
		mqtt.subscribe("x/y", (t, m) -> received.add("other"));

		// 서로 다른 topic 사이의 전달 순서는 보장하지 않는다.
		mqtt.dispatch("a/b", message("v"));
		Assertions.assertEquals(Set.of("exact", "plus", "hash"), Set.copyOf(received));
		Assertions.assertEquals(3, received.size());
	}

	@Test
	public void testFailingSubscriberDoesNotStopOthers() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		List<String> received = new CopyOnWriteArrayList<>();

		mqtt.subscribe("t", (t, m) -> { throw new IllegalStateException("boom"); });
		mqtt.subscribe("t", (t, m) -> received.add(new String(m.getPayload(), StandardCharsets.UTF_8)));

		mqtt.dispatch("t", message("v1"));
		Assertions.assertEquals(List.of("v1"), received);
	}

	@Test
	public void testUnsubscribeKeepsOtherSubscribersOfSameTopic() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		List<String> received = new CopyOnWriteArrayList<>();
		MqttService.Subscriber first = (t, m) -> received.add("first");
		MqttService.Subscriber second = (t, m) -> received.add("second");

		mqtt.subscribe("t", first);
		mqtt.subscribe("t", second);
		mqtt.unsubscribe("t", first);

		mqtt.dispatch("t", message("v"));
		Assertions.assertEquals(List.of("second"), received);

		mqtt.unsubscribe("t", second);
		mqtt.dispatch("t", message("v"));
		Assertions.assertEquals(List.of("second"), received);
	}

	@Test
	public void testDispatchWhileSubscriptionsChange() throws Exception {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		for ( int i = 0; i < 50; ++i ) {
			mqtt.subscribe("t", (t, m) -> { });
		}

		// 메시지 분배 도중에 다른 thread가 구독 목록을 바꾸어도 예외가 발생하지 않아야 한다.
		AtomicBoolean done = new AtomicBoolean(false);
		AtomicReference<Throwable> error = new AtomicReference<>();
		Thread churn = new Thread(() -> {
			while ( !done.get() ) {
				MqttService.Subscriber sub = (t, m) -> { };
				mqtt.subscribe("t", sub);
				mqtt.unsubscribe("t", sub);
			}
		});
		churn.start();
		try {
			for ( int i = 0; i < 20_000; ++i ) {
				mqtt.dispatch("t", message("v"));
			}
		}
		catch ( Throwable e ) {
			error.set(e);
		}
		finally {
			done.set(true);
			churn.join();
		}
		Assertions.assertNull(error.get());
	}

	@Test
	public void testInvalidQos() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		Assertions.assertThrows(IllegalArgumentException.class, () -> mqtt.subscribe("t", 3, (t, m) -> { }));
	}

	@Test
	public void testStopWhileConnecting() throws Exception {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		mqtt.startAsync().awaitRunning(Duration.ofSeconds(5));

		mqtt.stopAsync().awaitTerminated(Duration.ofSeconds(10));
		Assertions.assertEquals(State.TERMINATED, mqtt.state());
		Assertions.assertFalse(mqtt.isConnected());
	}

	@Test
	public void testPublishWithTimeoutWhenNotConnected() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		MqttException e = Assertions.assertThrows(MqttException.class,
										() -> mqtt.publish("t", message("v"), Duration.ofSeconds(1)));
		Assertions.assertEquals(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED, e.getReasonCode());
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> mqtt.publish("t", message("v"), Duration.ZERO));
	}
	
	@Test
	public void testMatchTopic() {
		// 빈 레벨은 하나의 레벨로 본다.
		Assertions.assertTrue(MqttService.matchTopic("a/+", "a/"));
		Assertions.assertFalse(MqttService.matchTopic("a", "a/"));
		Assertions.assertTrue(MqttService.matchTopic("a/+/b", "a//b"));
		Assertions.assertTrue(MqttService.matchTopic("+/+", "/"));
		Assertions.assertFalse(MqttService.matchTopic("+", "/"));
		// '#'는 상위 레벨 자체와도 일치한다.
		Assertions.assertTrue(MqttService.matchTopic("a/#", "a"));
		Assertions.assertTrue(MqttService.matchTopic("a/#", "a/b/c"));
		Assertions.assertTrue(MqttService.matchTopic("#", "/"));
		Assertions.assertFalse(MqttService.matchTopic("a/#", "b"));
		// 일반 비교
		Assertions.assertTrue(MqttService.matchTopic("a/b", "a/b"));
		Assertions.assertFalse(MqttService.matchTopic("a/b", "a/b/c"));
		Assertions.assertFalse(MqttService.matchTopic("a/+", "a/b/c"));
		// '$'로 시작하는 topic은 첫 레벨 와일드카드와 일치하지 않는다.
		Assertions.assertFalse(MqttService.matchTopic("#", "$SYS/broker"));
		Assertions.assertFalse(MqttService.matchTopic("+/broker", "$SYS/broker"));
		Assertions.assertTrue(MqttService.matchTopic("$SYS/#", "$SYS/broker"));
	}

	@Test
	public void testDispatchDoesNotDeliverToWrongEmptyLevel() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		List<String> received = new CopyOnWriteArrayList<>();
		mqtt.subscribe("a", (t, m) -> received.add("a"));
		mqtt.subscribe("a/+", (t, m) -> received.add("a/+"));

		mqtt.dispatch("a/", message("v"));
		Assertions.assertEquals(List.of("a/+"), received);
	}

	@Test
	public void testInvalidTopicIsRejectedWithoutStateChange() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		for ( String topic: new String[] { "", "a/#/b", "a+", "a#" } ) {
			IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> mqtt.subscribe(topic, 1, (t, m) -> { }), topic);
			Assertions.assertTrue(e.getMessage().contains("invalid MQTT topic filter"), e.getMessage());
			Assertions.assertEquals(0, mqtt.getSubscriberCount(topic));
		}
		Assertions.assertTrue(mqtt.getTopicQos().isEmpty());
	}

	@Test
	public void testSubscriptionHandle() {
		MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
		List<String> received = new CopyOnWriteArrayList<>();
		MqttService.Subscription first = mqtt.subscribe("t", 0, (t, m) -> received.add("first"));
		MqttService.Subscription second = mqtt.subscribe("t", 2, (t, m) -> received.add("second"));
		Assertions.assertEquals(2, mqtt.getTopicQos().get("t"));		// 요청된 QoS 중 최댓값

		first.close();
		first.close();		// 두 번째 호출은 아무것도 하지 않는다.
		Assertions.assertTrue(first.isClosed());
		Assertions.assertEquals(1, mqtt.getSubscriberCount("t"));
		
		mqtt.dispatch("t", message("v"));
		Assertions.assertEquals(List.of("second"), received);
		
		Assertions.assertTrue(second.unsubscribe());
		Assertions.assertEquals(0, mqtt.getSubscriberCount("t"));
		Assertions.assertTrue(mqtt.getTopicQos().isEmpty());
	}

	@Test
	public void testMessageArrivedLogHasNoPayload() throws Exception {
		ch.qos.logback.classic.Logger logger
						= (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(MqttService.class);
		ch.qos.logback.classic.Level level = logger.getLevel();
		ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender
																= new ch.qos.logback.core.read.ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
		try {
			MqttService mqtt = new MqttService(UNREACHABLE_BROKER);
			mqtt.getCallback().messageArrived("t", message("SENSITIVE_MQTT_PROBE"));
			
			List<String> logs = appender.list.stream().map(ev -> ev.getFormattedMessage()).toList();
			Assertions.assertTrue(logs.stream().anyMatch(msg -> msg.contains("bytes=20")), logs.toString());
			Assertions.assertTrue(logs.stream().noneMatch(msg -> msg.contains("SENSITIVE_MQTT_PROBE")), logs.toString());
		}
		finally {
			logger.detachAppender(appender);
			logger.setLevel(level);
		}
	}
	
	private static MqttMessage message(String payload) {
		return new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
	}
}
