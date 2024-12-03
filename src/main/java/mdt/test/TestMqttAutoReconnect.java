package mdt.test;

import java.time.Duration;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.LoggerFactory;

import mdt.client.support.AutoReconnectingMqttClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMqttAutoReconnect {
	public static final void main(String... args) throws Exception {
		Logger root = (Logger)LoggerFactory.getLogger("mdt");
		root.setLevel(Level.DEBUG);
		
		AutoReconnectingMqttClient reconnect
								= new AutoReconnectingMqttClient("tcp://0.0.0.0:1883", null, Duration.ofSeconds(1)) {
			@Override protected void mqttBrokerConnected(MqttClient client) { }
			@Override protected void mqttBrokerDisconnected() { }
		};
		reconnect.startAsync();
		
		for ( int i =0; i < 60; ++i ) {
			System.out.println(reconnect.pollMqttClient());
			Thread.sleep(1000);
		}
		
		reconnect.stopAsync();
		
		System.out.println("done");
	}
}
