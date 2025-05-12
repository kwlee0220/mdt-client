package mdt.test;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMqtt implements MqttCallback {
	@SuppressWarnings({ "unused", "resource" })
	public static final void main(String... args) throws Exception {
		MqttConnectOptions opts = new MqttConnectOptions();
		opts.setCleanSession(true);
		int qos = 1;
		
		String clientId = UUID.randomUUID().toString();
		MqttClient mqttClient = new MqttClient("tcp://0.0.0.0:1883", clientId, new MemoryPersistence());
		mqttClient.setCallback(new TestMqtt());
		mqttClient.connect(opts);
		
		mqttClient.subscribe("/mdt/#", 1);
		
		MqttMessage msg = new MqttMessage("tt".getBytes());
		msg.setQos(1);
		mqttClient.publish("/mdt", msg);
		mqttClient.publish("/mdt", msg);
		mqttClient.publish("/mdt", msg);
		mqttClient.publish("/mdt", msg);
		
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println(cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String msg = new String(message.getPayload());
		System.out.println(topic + ", " + msg);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println(token.getMessageId());
	}
}
