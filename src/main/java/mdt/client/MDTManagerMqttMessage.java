package mdt.client;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.Data;

/**
 * MDTManager의 MQTT 토픽({@code /mdt/manager/instances/#})으로 수신된 메시지.
 * <p>
 * {@link HttpMDTManager}가 MQTT 메시지를 수신하면 이 객체로 감싸 {@link HttpMDTManager#register(Object)}로
 * 등록된 구독자들에게 전달한다. Lombok {@code @Data}에 의해 {@code getTopic()}, {@code getMessage()} 등이
 * 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
public class MDTManagerMqttMessage {
	private final String topic;
	private final MqttMessage message;
	
	/**
	 * 메시지 객체를 생성한다.
	 *
	 * @param topic	메시지가 수신된 MQTT 토픽.
	 * @param message	수신된 MQTT 메시지.
	 */
	public MDTManagerMqttMessage(String topic, MqttMessage message) {
		this.topic = topic;
		this.message = message;
	}
}
