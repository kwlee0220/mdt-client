package mdt.workflow;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.Preconditions;
import utils.func.Optionals;


/**
 * 워크플로우를 구성하는 단일 task 노드의 실행 상태와 의존 관계를 표현하는 불변 값 객체.
 * <p>
 * {@link #getTaskId() taskId}와 {@link #getStatus() status}는 절대 {@code null}일 수 없다.
 * 동등성(equality)은 taskId만으로 판정하며, 동일한 taskId를 갖는 두 인스턴스는 다른 필드 값과
 * 무관하게 동등하게 취급된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"taskId", "status", "dependents", "startTime", "finishTime"})
public final class NodeTask {
	private final String m_taskId;
	private final WorkflowStatus m_status;
	private final Set<String> m_dependents;

	private final LocalDateTime m_startTime;
	private final LocalDateTime m_finishTime;

	/**
	 * {@code NodeTask} 인스턴스를 생성한다.
	 * <p>
	 * Jackson 역직렬화에서도 사용되는 생성자이다. 입력 {@code dependents}가 {@code null}이면
	 * 빈 {@link HashSet}으로 대체되어 저장된다.
	 *
	 * @param taskId     task를 워크플로우 내에서 식별하는 ID. {@code null}이 아니어야 한다.
	 * @param status     현재 실행 상태. {@code null}이 아니어야 한다.
	 * @param dependents 이 task가 시작되기 전에 완료되어 있어야 하는 선행 task의 ID 집합.
	 *                   {@code null}이면 빈 집합으로 대체된다.
	 * @param startTime  실행이 시작된 시각. 아직 시작 전이면 {@code null}.
	 * @param finishTime 실행이 종료된 시각. 아직 종료 전이면 {@code null}.
	 * @throws IllegalArgumentException {@code taskId} 또는 {@code status}가 {@code null}인 경우.
	 */
	public NodeTask(@JsonProperty("taskId") String taskId,
					@JsonProperty("status") WorkflowStatus status,
					@JsonProperty("dependents") Set<String> dependents,
					@JsonProperty("startTime") LocalDateTime startTime,
					@JsonProperty("finishTime") LocalDateTime finishTime) {
		Preconditions.checkNotNullArgument(taskId, "taskId is null");
		Preconditions.checkNotNullArgument(status, "status is null");

		m_taskId = taskId;
		m_status = status;
		m_dependents = Optionals.getOrElse(dependents, HashSet::new);
		m_startTime = startTime;
		m_finishTime = finishTime;
	}

	/**
	 * task의 식별자를 반환한다.
	 *
	 * @return 워크플로우 내에서 이 task를 유일하게 식별하는 ID.
	 */
	public String getTaskId() {
		return m_taskId;
	}

	/**
	 * task의 현재 실행 상태를 반환한다.
	 *
	 * @return {@link WorkflowStatus} 값. 절대 {@code null}이 아니다.
	 */
	public WorkflowStatus getStatus() {
		return m_status;
	}

	/**
	 * 이 task가 실행되기 전에 완료되어 있어야 하는 선행 task들의 ID 집합을 반환한다.
	 * <p>
	 * 반환되는 집합은 내부 필드를 그대로 노출하므로, 외부에서의 수정은 객체의 상태를 변경하게 된다.
	 * 호출자는 이를 수정해서는 안 된다.
	 *
	 * @return 선행 task ID 집합. 선행 task가 없으면 빈 집합.
	 */
	public Set<String> getDependents() {
		return m_dependents;
	}

	/**
	 * task의 실행 시작 시각을 반환한다.
	 *
	 * @return 시작 시각. 아직 시작되지 않았다면 {@code null}.
	 */
	public LocalDateTime getStartTime() {
		return m_startTime;
	}

	/**
	 * task의 실행 종료 시각을 반환한다.
	 *
	 * @return 종료 시각. 아직 종료되지 않았다면 {@code null}.
	 */
	public LocalDateTime getFinishTime() {
		return m_finishTime;
	}

	/**
	 * 두 {@code NodeTask}가 동등한지 비교한다.
	 * <p>
	 * 비교는 {@link #getTaskId() taskId}만을 기준으로 수행되며, 다른 필드는 고려하지 않는다.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}

		NodeTask other = (NodeTask)obj;
		return m_taskId.equals(other.m_taskId);
	}

	/**
	 * {@link #equals(Object) equals}와 일관되도록 {@link #getTaskId() taskId} 기반으로
	 * 해시 코드를 계산한다.
	 */
	@Override
	public int hashCode() {
		return m_taskId.hashCode();
	}

	/**
	 * 디버깅 용도의 문자열 표현을 반환한다. taskId와 현재 상태를 포함한다.
	 */
	@Override
	public String toString() {
		return String.format("NodeTask[name=%s, status=%s]", m_taskId, m_status);
	}
}
