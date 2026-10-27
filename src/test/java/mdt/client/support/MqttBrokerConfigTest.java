package mdt.client.support;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MqttBrokerConfigTest {
	@Test
	public void testUnknownFieldsAreIgnored() throws Exception {
		// 'connectTimeout'(오타)와 'retryInterval'(미지원)은 경고 로그만 남기고 무시된다.
		JsonNode jnode = JsonMapper.builder().build().readTree("""
			{ "brokerUrl": "tcp://broker:1883", "connectionTimeout": "5s",
			  "connectTimeout": "3s", "retryInterval": "10s" }
			""");
		MqttBrokerConfig config = MDTModelSerDe.readValue(jnode, MqttBrokerConfig.class);

		Assertions.assertEquals("tcp://broker:1883", config.getBrokerUrl());
		Assertions.assertEquals("5s", config.getConnectionTimeout());
		Assertions.assertNull(config.getKeepAliveInterval());
		Assertions.assertNull(config.getReconnectInterval());
	}

	@Test
	public void testReconnectInterval() throws Exception {
		JsonNode jnode = JsonMapper.builder().build().readTree("""
			{ "brokerUrl": "tcp://broker:1883", "reconnectInterval": "10s" }
			""");
		MqttBrokerConfig config = MDTModelSerDe.readValue(jnode, MqttBrokerConfig.class);
		Assertions.assertEquals("10s", config.getReconnectInterval());
		
		// 첫 연결 재시도 간격과 Paho 자동 재연결 간격의 상한에 함께 적용된다.
		MqttService mqtt = new MqttService(config);
		Assertions.assertEquals(Duration.ofSeconds(10), mqtt.getReconnectInterval());
		Assertions.assertEquals(10_000, mqtt.getConnectOptions().getMaxReconnectDelay());
		
		// 생략하면 기존 기본값(첫 연결 5초, Paho 최대 128초)을 사용한다.
		MqttService dflt = new MqttService(new MqttBrokerConfig());
		Assertions.assertEquals(Duration.ofSeconds(5), dflt.getReconnectInterval());
		Assertions.assertEquals(128_000, dflt.getConnectOptions().getMaxReconnectDelay());
	}

	@Test
	public void testSerializeOnlyKnownFields() throws Exception {
		MqttBrokerConfig config = new MqttBrokerConfig();
		config.setBrokerUrl("tcp://broker:1883");
		config.setKeepAliveInterval("30s");

		JsonNode written = MDTModelSerDe.getJsonMapper().valueToTree(config);
		Assertions.assertEquals("tcp://broker:1883", written.get("brokerUrl").asText());
		Assertions.assertEquals("30s", written.get("keepAliveInterval").asText());
		written.fieldNames().forEachRemaining(name ->
			Assertions.assertTrue(name.equals("brokerUrl") || name.equals("connectionTimeout")
									|| name.equals("keepAliveInterval") || name.equals("reconnectInterval"), name));
	}
}
