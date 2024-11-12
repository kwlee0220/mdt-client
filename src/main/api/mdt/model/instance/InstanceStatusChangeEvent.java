package mdt.model.instance;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.func.FOption;

import mdt.model.MDTManagerEvent;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class InstanceStatusChangeEvent extends MDTManagerEvent {
	private final String m_instanceId;
	private final MDTInstanceStatus m_status;
	@Nullable private final String m_serviceEndpoint;
	
	@JsonCreator
	private InstanceStatusChangeEvent(@JsonProperty("eventType") String eventType,
										@JsonProperty("instanceId") String instanceId,
										@JsonProperty("status") MDTInstanceStatus status,
										@JsonProperty("serviceEndpoint") String ep) {
		super(eventType);
		
		m_instanceId = instanceId;
		m_status = status;
		m_serviceEndpoint = ep;
	}

	private InstanceStatusChangeEvent(@JsonProperty("instanceId") String instanceId,
										@JsonProperty("status") MDTInstanceStatus status,
										@JsonProperty("serviceEndpoint") String ep) {
		super(InstanceStatusChangeEvent.class.getName());
		
		m_instanceId = instanceId;
		m_status = status;
		m_serviceEndpoint = ep;
	}
	
	@JsonProperty("instanceId")
	public String getInstanceId() {
		return m_instanceId;
	}
	
	@JsonProperty("status")
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	@JsonProperty("serviceEndpoint")
	public String getServiceEndpoint() {
		return m_serviceEndpoint;
	}
	
	@Override
	public String toString() {
		String epStr = FOption.mapOrElse(this.m_serviceEndpoint, ep -> String.format(", endpoint=%s", ep), ""); 
		return String.format("InstanceStatusChangeEvent(instanceId=%s, status=%s%s)",
							this.m_instanceId, this.m_status, epStr);
	}

	public static InstanceStatusChangeEvent ADDING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.ADDING, null);
	}
	public static InstanceStatusChangeEvent ADD_FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.REMOVED, null);
	}
	public static InstanceStatusChangeEvent ADDED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.STOPPED, null);
	}
	public static InstanceStatusChangeEvent STOPPED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.STOPPED, null);
	}
	public static InstanceStatusChangeEvent STARTING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.STARTING, null);
	}
	public static InstanceStatusChangeEvent RUNNING(String instanceId, String svcEp) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.RUNNING, svcEp);
	}
	public static InstanceStatusChangeEvent STOPPING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.STOPPING, null);
	}
	public static InstanceStatusChangeEvent FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.FAILED, null);
	}
	public static InstanceStatusChangeEvent REMOVED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, MDTInstanceStatus.REMOVED, null);
	}
}
