package mdt.workflow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import utils.Preconditions;
import utils.Split;
import utils.func.Optionals;
import utils.stream.FStream;

/**
 * 워크플로우의 실행 메타데이터와 구성 task들을 표현하는 불변 값 객체.
 * <p>
 * 이름, 모델 ID, 현재 실행 상태, 생성/시작/종료 시각, 그리고 워크플로우를 구성하는
 * {@link NodeTask} 목록을 보유한다. 인스턴스는 {@link Builder}를 통해 생성하거나
 * Jackson을 통한 JSON 역직렬화로 생성된다.
 * <p>
 * {@link #getName() name}, {@link #getModelId() modelId}, {@link #getStatus() status}는 절대
 * {@code null}일 수 없다. 동등성(equality)은 {@code name}과 {@code modelId}의 쌍으로 판정된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties({"name", "modelId", "status", "creationTime", "startTime", "finishTime", "tasks"})
public final class Workflow {
	private final String m_name;
	private final String m_modelId;
	private final WorkflowStatus m_status;
	private final LocalDateTime m_creationTime;
	private final LocalDateTime m_startTime;
	private final LocalDateTime m_finishTime;
	private final List<NodeTask> m_tasks;

	/**
	 * 모든 필드를 명시적으로 받는 생성자. 주로 Jackson 역직렬화에서 사용된다.
	 * <p>
	 * 입력 {@code tasks}가 {@code null}이면 빈 리스트로 정규화되어 저장된다.
	 *
	 * @param name         워크플로우 이름. {@code null}이 아니어야 한다.
	 * @param modelId      워크플로우가 인스턴스화한 모델의 ID. {@code null}이 아니어야 한다.
	 * @param status       현재 실행 상태. {@code null}이 아니어야 한다.
	 * @param creationTime 워크플로우가 생성된 시각.
	 * @param startTime    실행이 시작된 시각. 아직 시작 전이면 {@code null}.
	 * @param finishTime   실행이 종료된 시각. 아직 종료 전이면 {@code null}.
	 * @param tasks        워크플로우를 구성하는 {@link NodeTask} 목록.
	 *                     {@code null}이면 빈 리스트로 대체된다.
	 * @throws IllegalArgumentException {@code name}, {@code modelId}, {@code status} 중 하나라도
	 *                                  {@code null}인 경우.
	 */
	public Workflow(@JsonProperty("name") String name,
					@JsonProperty("modelId") String modelId,
					@JsonProperty("status") WorkflowStatus status,
					@JsonProperty("creationTime") LocalDateTime creationTime,
					@JsonProperty("startTime") LocalDateTime startTime,
					@JsonProperty("finishTime") LocalDateTime finishTime,
					@JsonProperty("tasks") List<NodeTask> tasks) {
		Preconditions.checkNotNullArgument(name, "name is null");
		Preconditions.checkNotNullArgument(modelId, "modelId is null");
		Preconditions.checkNotNullArgument(status, "status is null");

		m_name = name;
		m_modelId = modelId;
		m_status = status;
		m_creationTime = creationTime;
		m_startTime = startTime;
		m_finishTime = finishTime;
//		m_tasks = sortTopologically(tasks);
		m_tasks = Optionals.getOrElse(tasks, Lists::newArrayList);
	}

	/**
	 * {@link Builder}로부터 인스턴스를 생성한다.
	 * <p>
	 * 빌더의 {@code modelId}가 설정되지 않은 경우, {@code name}의 마지막 {@code "-"} 앞부분으로
	 * 자동 추론된다. 빌더의 {@code tasks}가 {@code null}이면 빈 리스트로 정규화된다.
	 *
	 * @param builder 모든 필수 필드를 구성한 빌더 인스턴스. {@code null}이 아니어야 하며,
	 *                {@code name}과 {@code status}가 설정되어 있어야 한다.
	 * @throws IllegalArgumentException {@code builder}가 {@code null}이거나, {@code builder}의
	 *                                  {@code name} 또는 {@code status}가 {@code null}인 경우.
	 */
	private Workflow(Builder builder) {
		Preconditions.checkNotNullArgument(builder, "Workflow.Builder is null");
		Preconditions.checkNotNullArgument(builder.m_name, "Name is null");
		Preconditions.checkNotNullArgument(builder.m_status, "Status is null");

		m_name = builder.m_name;
		m_modelId = Optionals.getOrElse(builder.m_modelId, () -> Split.splitLast(m_name, "-").head());
		m_status = builder.m_status;
		m_creationTime = builder.m_creationTime;
		m_startTime = builder.m_startTime;
		m_finishTime = builder.m_finishTime;
//		m_tasks = sortTopologically(builder.m_tasks);
		m_tasks = Optionals.getOrElse(builder.m_tasks, Lists::newArrayList);
	}

	/**
	 * 워크플로우 이름을 반환한다.
	 *
	 * @return 워크플로우 이름. 절대 {@code null}이 아니다.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * 워크플로우가 인스턴스화한 모델의 ID를 반환한다.
	 *
	 * @return 모델 ID. 절대 {@code null}이 아니다.
	 */
	public String getModelId() {
		return m_modelId;
	}

	/**
	 * 현재 실행 상태를 반환한다.
	 *
	 * @return {@link WorkflowStatus} 값. 절대 {@code null}이 아니다.
	 */
	public WorkflowStatus getStatus() {
		return m_status;
	}

	/**
	 * 워크플로우가 생성된 시각을 반환한다.
	 *
	 * @return 생성 시각.
	 */
	public LocalDateTime getCreationTime() {
		return m_creationTime;
	}

	/**
	 * 실행이 시작된 시각을 반환한다.
	 *
	 * @return 시작 시각. 아직 시작되지 않았다면 {@code null}.
	 */
	public LocalDateTime getStartTime() {
		return m_startTime;
	}

	/**
	 * 실행이 종료된 시각을 반환한다.
	 *
	 * @return 종료 시각. 아직 종료되지 않았다면 {@code null}.
	 */
	public LocalDateTime getFinishTime() {
		return m_finishTime;
	}

	/**
	 * 워크플로우를 구성하는 {@link NodeTask}들의 목록을 반환한다.
	 * <p>
	 * 반환되는 리스트는 내부 필드를 그대로 노출하므로, 외부에서의 수정은 객체의 상태를 변경하게 된다.
	 * 호출자는 이를 수정해서는 안 된다.
	 *
	 * @return task 목록. task가 없으면 빈 리스트.
	 */
	public List<NodeTask> getTasks() {
		return m_tasks;
	}

	/**
	 * 두 {@code Workflow}가 동등한지 비교한다.
	 * <p>
	 * 비교는 {@link #getName() name}과 {@link #getModelId() modelId}의 쌍을 기준으로 수행되며,
	 * 상태나 시간, task 목록 등 다른 필드는 고려하지 않는다.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}

		Workflow other = (Workflow)obj;
		return m_name.equals(other.m_name) && m_modelId.equals(other.m_modelId);
	}

	/**
	 * {@link #equals(Object) equals}와 일관되도록 {@link #getName() name}과
	 * {@link #getModelId() modelId} 기반으로 해시 코드를 계산한다.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(m_name, m_modelId);
	}

	/**
	 * 디버깅 용도의 문자열 표현을 반환한다. 이름, modelId, 상태, 그리고 각 task의 ID와 상태를 포함한다.
	 */
	@Override
	public String toString() {
		String nodesStatusStr = FStream.from(m_tasks)
										.map(nt -> String.format("%s(%s)", nt.getTaskId(), nt.getStatus()))
										.join(", ");
		return String.format("Workflow[name=%s, model=%s, status=%s, tasks={%s}]",
							m_name, m_modelId, m_status, nodesStatusStr);
	}

	/**
	 * 의존성 순서에 따라 task들을 위상 정렬한다.
	 * <p>
	 * 각 {@link NodeTask#getDependents()}에 명시된 선행 task가 결과 리스트에서 먼저 위치하도록
	 * 정렬한다. 순환 의존성이 있거나 입력 목록에 존재하지 않는 task ID를 의존성으로 가진 경우,
	 * 한 라운드 안에 단 한 개의 task도 정렬되지 못함을 감지하여 예외를 던진다.
	 *
	 * @param nodeTasks 정렬할 task 목록.
	 * @return 위상 순서로 정렬된 task 목록.
	 * @throws IllegalStateException 순환 의존성 또는 미해결 의존성이 검출된 경우.
	 */
	@SuppressWarnings("unused")
	private static List<NodeTask> sortTopologically(List<NodeTask> nodeTasks) {
		List<NodeTask> remains = Lists.newArrayList(nodeTasks);
		List<NodeTask> sorted = Lists.newArrayList();
		Set<String> sortedNames = FStream.from(sorted)
                                           .map(NodeTask::getTaskId)
                                           .toSet();

		int consecutiveDefers = 0;
		while ( remains.size() > 0 ) {
			NodeTask task = remains.remove(0);

			if ( FStream.from(task.getDependents())
						.exists(t -> !sortedNames.contains(t)) ) {
				remains.add(task);
				if ( ++consecutiveDefers >= remains.size() ) {
					throw new IllegalStateException(
							"Cannot topologically sort tasks: circular or missing dependency among "
							+ remains);
				}
			}
			else {
				sorted.add(task);
				sortedNames.add(task.getTaskId());
				consecutiveDefers = 0;
			}
		}

		return sorted;
	}

	/**
	 * 새 {@link Builder} 인스턴스를 반환한다.
	 *
	 * @return 빈 빌더.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link Workflow}를 단계적으로 구성하기 위한 빌더.
	 */
	public static class Builder {
		private String m_name;
		private String m_modelId;
		private WorkflowStatus m_status;
		private LocalDateTime m_creationTime;
		private LocalDateTime m_startTime;
		private LocalDateTime m_finishTime;
		private final List<NodeTask> m_tasks = Lists.newArrayList();

		private Builder() { }

		/**
		 * 현재까지 설정된 값으로 {@link Workflow} 인스턴스를 생성한다.
		 * <p>
		 * {@code modelId}가 설정되지 않은 경우, {@code name}의 마지막 {@code "-"} 앞부분으로
		 * 자동 추론된다.
		 *
		 * @return 새로 생성된 {@link Workflow}.
		 * @throws IllegalArgumentException {@code name} 또는 {@code status}가 설정되지 않은 경우.
		 */
		public Workflow build() {
			return new Workflow(this);
		}

		/**
		 * 워크플로우 이름을 설정한다.
		 * <p>
		 * 필수 항목이며, {@link #build()} 호출 전에 반드시 설정되어야 한다. {@code modelId}가
		 * 따로 설정되지 않은 경우 {@link #build()} 시점에 이 이름의 마지막 {@code "-"} 앞부분으로
		 * 자동 추론된다.
		 *
		 * @param name 워크플로우 이름. {@code null}이 아니어야 한다.
		 * @return 이 빌더.
		 * @throws IllegalArgumentException {@code name}이 {@code null}인 경우.
		 */
		public Builder name(String name) {
			Preconditions.checkNotNullArgument(name, "name is null");

			m_name = name;
			return this;
		}

		/**
		 * 모델 ID를 명시적으로 설정한다.
		 * <p>
		 * 설정하지 않으면 {@link #build()} 시점에 {@code name}으로부터 자동 추론된다. 이 메서드로
		 * 값을 지정하면 자동 추론을 건너뛰고 지정한 값이 그대로 사용된다.
		 *
		 * @param id 모델 ID.
		 * @return 이 빌더.
		 */
		public Builder modelId(String id) {
			m_modelId = id;
			return this;
		}

		/**
		 * 현재 실행 상태를 설정한다.
		 * <p>
		 * 필수 항목이며, {@link #build()} 호출 전에 반드시 설정되어야 한다.
		 *
		 * @param status {@link WorkflowStatus} 값.
		 * @return 이 빌더.
		 */
		public Builder status(WorkflowStatus status) {
			m_status = status;
			return this;
		}

		/**
		 * 생성 시각을 설정한다.
		 *
		 * @param ldt 생성 시각.
		 * @return 이 빌더.
		 */
		public Builder creationTime(LocalDateTime ldt) {
			m_creationTime = ldt;
			return this;
		}

		/**
		 * 실행 시작 시각을 설정한다.
		 *
		 * @param ldt 시작 시각.
		 * @return 이 빌더.
		 */
		public Builder startTime(LocalDateTime ldt) {
			m_startTime = ldt;
			return this;
		}

		/**
		 * 실행 종료 시각을 설정한다.
		 *
		 * @param ldt 종료 시각.
		 * @return 이 빌더.
		 */
		public Builder finishTime(LocalDateTime ldt) {
			m_finishTime = ldt;
			return this;
		}

		/**
		 * task 목록을 설정한다.
		 * <p>
		 * 기존에 설정된 task들은 모두 제거되고 입력 목록으로 완전히 대체된다. 따라서 이 메서드를
		 * 여러 번 호출하면 마지막 호출의 task 목록만 유지된다.
		 *
		 * @param tasks 설정할 task 목록. {@code null}이 아니어야 한다.
		 * @return 이 빌더.
		 * @throws IllegalArgumentException {@code tasks}가 {@code null}인 경우.
		 */
		public Builder tasks(List<NodeTask> tasks) {
			Preconditions.checkNotNullArgument(tasks, "tasks is null");

			m_tasks.clear();
			m_tasks.addAll(tasks);
			return this;
		}
	}
}
