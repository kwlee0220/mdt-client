package mdt.client.instance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.Preconditions;

import mdt.client.support.MqttService;
import mdt.client.support.MqttService.Subscriber;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.InstanceStatusChangeEvent;

/**
 * MQTT broker로부터 MDTInstance 상태 변경 메시지를 구독하여
 * {@link HttpMDTInstanceManager#EVENT_BUS}로 전파하는 {@link MqttService}.
 * <p>
 * 생성 시 {@code /mdt/manager/instances/#} 토픽을 구독하며, 수신된 메시지의 payload(JSON)를
 * {@link InstanceStatusChangeEvent}로 파싱하여 이벤트 버스에 게시한다.
 * 형식이 잘못된 메시지는 경고 로그를 남기고 무시한다.
 * <p>
 * 객체는 {@link #builder()}로 생성한다. broker와의 실제 연결은 {@link MqttService}의
 * 서비스 시작({@code startAsync()}) 이후에 이루어진다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceStatusSubscriber extends MqttService implements Subscriber {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTInstanceStatusSubscriber.class);
	private static final String TOPIC_PATTERN = "/mdt/manager/instances/#";
	private static final JsonMapper MAPPER = MDTModelSerDe.MAPPER;
	
	private final int m_qos;
	
	private MDTInstanceStatusSubscriber(Builder builder) {
		super(builder.m_mqttServerUri);
		Preconditions.checkArgument(builder.m_qos >= 0 && builder.m_qos <= 2,
									"invalid QoS: %d", builder.m_qos);
		
		m_qos = builder.m_qos;
		subscribe(TOPIC_PATTERN, m_qos, this);
		
		setLogger(s_logger);
	}
	
	/**
	 * 수신된 상태 변경 메시지를 {@link InstanceStatusChangeEvent}로 변환하여
	 * {@link HttpMDTInstanceManager#EVENT_BUS}에 게시한다.
	 * <p>
	 * payload를 파싱할 수 없는 경우에는 경고 로그를 남기고 메시지를 무시한다.
	 *
	 * @param topic	메시지가 수신된 토픽.
	 * @param msg	수신된 MQTT 메시지.
	 */
	@Override
	public void onMessage(String topic, MqttMessage msg) {
		String json = new String(msg.getPayload(), StandardCharsets.UTF_8);
		try {
			InstanceStatusChangeEvent ev = MAPPER.readValue(json, InstanceStatusChangeEvent.class);
			
			HttpMDTInstanceManager.EVENT_BUS.post(ev);
		}
		catch ( IOException e ) {
			getLogger().warn("ignoring malformed message: topic={}, message={}, cause={}", topic, json, e.getMessage());
		}
	}
	
	/**
	 * {@link MDTInstanceStatusSubscriber} 객체를 생성하기 위한 빌더를 반환한다.
	 *
	 * @return	새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link MDTInstanceStatusSubscriber} 객체 생성을 위한 빌더.
	 */
	public static class Builder {
		private String m_mqttServerUri;
		private int m_qos = 0;
		
		/**
		 * 설정된 값으로 {@link MDTInstanceStatusSubscriber} 객체를 생성한다.
		 *
		 * @return	생성된 {@link MDTInstanceStatusSubscriber} 객체.
		 * @throws MqttException	선언만 되어 있으며, MQTT 클라이언트 생성 실패는
		 * 						{@link utils.InternalException}으로 던져진다.
		 * @throws IllegalArgumentException	MQTT 서버 URI가 {@code null}이거나 QoS가 0~2 범위가 아닌 경우.
		 */
		public MDTInstanceStatusSubscriber build() throws MqttException {
			return new MDTInstanceStatusSubscriber(this);
		}
		
		/**
		 * 접속할 MQTT broker의 URI를 설정한다.
		 *
		 * @param uri	MQTT broker URI.
		 * @return	본 빌더 객체.
		 */
		public Builder mqttServerUri(String uri) {
			m_mqttServerUri = uri;
			return this;
		}
		
		/**
		 * 상태 변경 토픽 구독에 사용할 QoS를 설정한다. 기본값은 0이다.
		 *
		 * @param qos	구독 QoS (0, 1, 2).
		 * @return	본 빌더 객체.
		 */
		public Builder qos(int qos) {
			m_qos = qos;
			return this;
		}
	}
}
