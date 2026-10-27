package mdt.client.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

	private static MqttMessage message(String payload) {
		return new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
	}
}
