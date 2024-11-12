package mdt.client;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.Data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
public class MDTManagerMqttMessage {
	private final String topic;
	private final MqttMessage message;
	
	public MDTManagerMqttMessage(String topic, MqttMessage message) {
		this.topic = topic;
		this.message = message;
	}
}
