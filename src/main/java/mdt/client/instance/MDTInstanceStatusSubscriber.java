package mdt.client.instance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import mdt.client.support.MqttService;
import mdt.client.support.MqttService.Subscriber;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.InstanceStatusChangeEvent;

/**
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
		Preconditions.checkArgument(builder.m_qos >= 0 && builder.m_qos <= 2);
		
		m_qos = builder.m_qos;
		subscribe(TOPIC_PATTERN, m_qos, this);
		
		setLogger(s_logger);
	}
	
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
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_mqttServerUri;
		private int m_qos = 0;
		
		public MDTInstanceStatusSubscriber build() throws MqttException {
			return new MDTInstanceStatusSubscriber(this);
		}
		
		public Builder mqttServerUri(String uri) {
			m_mqttServerUri = uri;
			return this;
		}
		
		public Builder qos(int qos) {
			m_qos = qos;
			return this;
		}
	}
}
