package mdt.workflow.model.port;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.Named;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class PortBinding implements Named {
	@JsonProperty("name") private final String m_name;
	@JsonProperty("from") private final FromDescriptor m_from;
	
	public PortBinding(@JsonProperty("name") String name,
						@JsonProperty("from") FromDescriptor from) {
		m_name = name;
		m_from = from;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public FromDescriptor getFrom() {
		return m_from;
	}

	public static class FromDescriptor {
		private final String m_taskId;
		private final String m_portName;
		
		@JsonCreator
		public FromDescriptor(@JsonProperty("task") String taskId,
								@JsonProperty("port") String portName) {
			m_taskId = taskId;
			m_portName = portName;
		}
		
		@JsonProperty("task")
		public String getTaskId() {
			return m_taskId;
		}
		
		@JsonProperty("port")
		public String getPortName() {
			return m_portName;
		}
	}
}
