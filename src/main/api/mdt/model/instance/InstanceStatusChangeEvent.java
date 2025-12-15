package mdt.model.instance;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.InternalException;

import mdt.model.MDTManagerEvent;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class InstanceStatusChangeEvent extends MDTManagerEvent {
	private final String m_instanceId;
	private final String m_statusChange;
	private final @Nullable String m_serviceEndpoint;
	
	@JsonCreator
	private InstanceStatusChangeEvent(@JsonProperty("eventType") String eventType,
										@JsonProperty("instanceId") String instanceId,
										@JsonProperty("statusChange") String status,
										@JsonProperty("serviceEndpoint") String ep) {
		super(eventType);
		
		m_instanceId = instanceId;
		m_statusChange = status;
		m_serviceEndpoint = ep;
	}

	private InstanceStatusChangeEvent(@JsonProperty("instanceId") String instanceId,
										@JsonProperty("statusChange") String status,
										@JsonProperty("serviceEndpoint") String ep) {
		super(InstanceStatusChangeEvent.class.getName());
		
		m_instanceId = instanceId;
		m_statusChange = status;
		m_serviceEndpoint = ep;
	}
	
	@JsonProperty("instanceId")
	public String getInstanceId() {
		return m_instanceId;
	}
	
	@JsonProperty("statusChange")
	public String getStatusChange() {
		return m_statusChange;
	}
	
	@JsonProperty("serviceEndpoint")
	public String getServiceEndpoint() {
		return m_serviceEndpoint;
	}
	
	@JsonIgnore
	public MDTInstanceStatus getInstanceStatus() {
		try {
			return MDTInstanceStatus.valueOf(m_statusChange);
		}
		catch ( Exception e ) {
			return switch ( m_statusChange ) {
				case "ADD_FAILED" -> MDTInstanceStatus.REMOVED;
				case "ADDED" -> MDTInstanceStatus.STOPPED;
				default -> throw new InternalException("Invalid statuc-change: " + m_statusChange);
			};
		}
	}
	
	@Override
	public String toString() {
		String epStr = (m_serviceEndpoint != null) ? String.format(", endpoint=%s", m_serviceEndpoint) : "";
		return String.format("InstanceStatusChangeEvent(instanceId=%s, statusChange=%s%s)",
							m_instanceId, m_statusChange, epStr);
	}

	public static InstanceStatusChangeEvent ADDING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADDING", null);
	}
	public static InstanceStatusChangeEvent ADD_FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADD_FAILED", null);
	}
	public static InstanceStatusChangeEvent ADDED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADDED", null);
	}
	public static InstanceStatusChangeEvent STOPPED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STOPPED", null);
	}
	public static InstanceStatusChangeEvent STARTING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STARTING", null);
	}
	public static InstanceStatusChangeEvent RUNNING(String instanceId, String svcEp) {
		return new InstanceStatusChangeEvent(instanceId, "RUNNING", svcEp);
	}
	public static InstanceStatusChangeEvent STOPPING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STOPPING", null);
	}
	public static InstanceStatusChangeEvent FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "FAILED", null);
	}
	public static InstanceStatusChangeEvent REMOVED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "REMOVED", null);
	}
}
