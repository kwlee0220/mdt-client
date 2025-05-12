package mdt.workflow;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties({"workflowName", "taskName", "status", "dependents", "startTime", "finishTime"})
public class NodeTask {
	private final String m_workflowName;
	private final String m_taskName;
	private final WorkflowStatus m_status;
	private final Set<String> m_dependents;

	private final LocalDateTime m_startTime;
	private final LocalDateTime m_finishTime;
	
	public NodeTask(@JsonProperty("workflowName") String workflowName,
					@JsonProperty("taskName") String taskName,
					@JsonProperty("status") WorkflowStatus status,
					@JsonProperty("dependents") Set<String> dependents,
					@JsonProperty("startTime") LocalDateTime startTime,
					@JsonProperty("finishTime") LocalDateTime finishTime) {
		m_workflowName = workflowName;
		m_taskName = taskName;
		m_status = status;
		m_dependents = dependents;
		m_startTime = startTime;
		m_finishTime = finishTime;
	}
	
	public String getWorkflowName() {
		return m_workflowName;
	}
	
	public String getTaskName() {
		return m_taskName;
	}
	
	public WorkflowStatus getStatus() {
		return m_status;
	}
	
	public Set<String> getDependents() {
		return m_dependents;
	}
	
	public LocalDateTime getStartTime() {
		return m_startTime;
	}
	
	public LocalDateTime getFinishTime() {
		return m_finishTime;
	}
	
	@Override
	public String toString() {
		return String.format("NodeTask[name=%s, status=%s]", m_taskName, m_status);
	}
}
