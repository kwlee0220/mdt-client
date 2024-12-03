package mdt.test;

import java.time.Duration;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.LoggerFactory;

import mdt.client.support.MqttBrokerReconnect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMqttReconnect {
	public static final void main(String... args) throws Exception {
		Logger root = (Logger)LoggerFactory.getLogger("mdt");
		root.setLevel(Level.DEBUG);
		
		MqttBrokerReconnect reconnect = MqttBrokerReconnect.builder()
															.mqttServerUri("tcp://0.0.0.0:1883")
															.reconnectTryInterval(Duration.ofSeconds(1))
															.build();
		reconnect.whenFinished(System.out::println);
		
		reconnect.start();
		MqttClient client = reconnect.get();
		System.out.println(client);
		
		client.disconnect();
		client.close(true);
	}
}
