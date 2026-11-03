package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.Nullable;

import utils.InternalException;

import mdt.model.MDTManagerEvent;


/**
 * MDTInstance의 상태 변화를 알리는 {@link MDTManagerEvent}.
 * <p>
 * 상태 변화는 문자열({@code statusChange})로 표현되며, {@link MDTInstanceStatus}의 상수 이름 외에
 * 등록 과정을 나타내는 {@code "ADDED"}, {@code "ADD_FAILED"}가 사용될 수 있다.
 * 이벤트 객체는 {@link #ADDING(String)}, {@link #RUNNING(String, String)} 등의 정적 팩토리 메소드로
 * 생성하며, JSON으로 직렬화될 때 {@code null}인 필드는 생략된다.
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
	
	/**
	 * 상태가 변경된 MDTInstance의 식별자를 반환한다.
	 *
	 * @return	MDTInstance 식별자.
	 */
	@JsonProperty("instanceId")
	public String getInstanceId() {
		return m_instanceId;
	}
	
	/**
	 * 상태 변화를 나타내는 문자열을 반환한다.
	 * <p>
	 * {@link MDTInstanceStatus}의 상수 이름이거나 {@code "ADDED"}, {@code "ADD_FAILED"}이다.
	 *
	 * @return	상태 변화 문자열.
	 * @see #getInstanceStatus()
	 */
	@JsonProperty("statusChange")
	public String getStatusChange() {
		return m_statusChange;
	}
	
	/**
	 * MDTInstance의 서비스 endpoint를 반환한다.
	 * <p>
	 * {@link #RUNNING(String, String)}으로 생성된 이벤트에서만 설정되며, 그 외에는 {@code null}이다.
	 *
	 * @return	서비스 endpoint. 설정되지 않은 경우 {@code null}.
	 */
	@JsonProperty("serviceEndpoint")
	public String getServiceEndpoint() {
		return m_serviceEndpoint;
	}
	
	/**
	 * 상태 변화 문자열({@link #getStatusChange()})을 {@link MDTInstanceStatus}로 변환하여 반환한다.
	 * <p>
	 * {@link MDTInstanceStatus}의 상수 이름이 아닌 경우, {@code "ADD_FAILED"}는
	 * {@link MDTInstanceStatus#REMOVED}로, {@code "ADDED"}는 {@link MDTInstanceStatus#STOPPED}로
	 * 변환된다.
	 *
	 * @return	변환된 MDTInstance 상태.
	 * @throws InternalException	상태 변화 문자열이 위 어느 경우에도 해당하지 않는 경우.
	 */
	@JsonIgnore
	public MDTInstanceStatus getInstanceStatus() {
		try {
			return MDTInstanceStatus.valueOf(m_statusChange);
		}
		catch ( Exception e ) {
			return switch ( m_statusChange ) {
				case "ADD_FAILED" -> MDTInstanceStatus.REMOVED;
				case "ADDED" -> MDTInstanceStatus.STOPPED;
				default -> throw new InternalException("Invalid status-change: " + m_statusChange);
			};
		}
	}
	
	@Override
	public String toString() {
		String epStr = (m_serviceEndpoint != null) ? String.format(", endpoint=%s", m_serviceEndpoint) : "";
		return String.format("InstanceStatusChangeEvent(instanceId=%s, statusChange=%s%s)",
							m_instanceId, m_statusChange, epStr);
	}

	/**
	 * MDTInstance가 등록 중({@code ADDING})임을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent ADDING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADDING", null);
	}

	/**
	 * MDTInstance의 등록이 실패({@code ADD_FAILED})했음을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent ADD_FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADD_FAILED", null);
	}

	/**
	 * MDTInstance의 등록이 완료({@code ADDED})되었음을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent ADDED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "ADDED", null);
	}

	/**
	 * MDTInstance가 정지({@code STOPPED})되었음을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent STOPPED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STOPPED", null);
	}

	/**
	 * MDTInstance가 시작 중({@code STARTING})임을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent STARTING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STARTING", null);
	}

	/**
	 * MDTInstance가 실행 중({@code RUNNING})임을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @param svcEp	MDTInstance의 서비스 endpoint.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent RUNNING(String instanceId, String svcEp) {
		return new InstanceStatusChangeEvent(instanceId, "RUNNING", svcEp);
	}

	/**
	 * MDTInstance가 정지 중({@code STOPPING})임을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent STOPPING(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "STOPPING", null);
	}

	/**
	 * MDTInstance가 실패({@code FAILED}) 상태가 되었음을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent FAILED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "FAILED", null);
	}

	/**
	 * MDTInstance가 제거({@code REMOVED})되었음을 알리는 이벤트를 생성한다.
	 *
	 * @param instanceId	MDTInstance 식별자.
	 * @return	생성된 이벤트 객체.
	 */
	public static InstanceStatusChangeEvent REMOVED(String instanceId) {
		return new InstanceStatusChangeEvent(instanceId, "REMOVED", null);
	}
}
