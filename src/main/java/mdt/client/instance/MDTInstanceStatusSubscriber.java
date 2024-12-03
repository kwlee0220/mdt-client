package mdt.client.instance;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import mdt.client.support.AutoReconnectingMqttClient;
import mdt.model.instance.InstanceStatusChangeEvent;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceStatusSubscriber extends AutoReconnectingMqttClient {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTInstanceStatusSubscriber.class);
	private static final String TOPIC_PATTERN = "/mdt/manager/instances/#";
	private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
	
	private final int m_qos;
	
	public MDTInstanceStatusSubscriber(Builder builder) {
		super(builder.m_mqttServerUri, builder.m_clientId, builder.m_reconnectTryInterval);
		Preconditions.checkArgument(builder.m_qos >= 0 && builder.m_qos <= 2);
		
		m_qos = builder.m_qos;
		setLogger(s_logger);
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		String json = new String(msg.getPayload(), StandardCharsets.UTF_8);
		InstanceStatusChangeEvent ev = MAPPER.readValue(json, InstanceStatusChangeEvent.class);
		
		HttpMDTInstanceManagerClient.EVENT_BUS.post(ev);
	}

	@Override public void deliveryComplete(IMqttDeliveryToken token) { }
	
	@Override protected void mqttBrokerConnected(MqttClient client) throws Exception {
		client.subscribe(TOPIC_PATTERN, m_qos);
	}
	@Override protected void mqttBrokerDisconnected() { }
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_mqttServerUri;
		private String m_clientId;
		private int m_qos = 0;
		private Duration m_reconnectTryInterval = Duration.ofSeconds(10);
		
		public MDTInstanceStatusSubscriber build() {
			return new MDTInstanceStatusSubscriber(this);
		}
		
		public Builder mqttServerUri(String uri) {
			m_mqttServerUri = uri;
			return this;
		}
		
		public Builder clientId(String id) {
			m_clientId = id;
			return this;
		}
		
		public Builder qos(int qos) {
			m_qos = qos;
			return this;
		}
		
		public Builder reconnectTryInterval(Duration interval) {
			m_reconnectTryInterval = interval;
			return this;
		}
	}
}
