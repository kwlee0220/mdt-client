package mdt.workflow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.Utilities;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties({"name", "modelId", "status", "creationTime", "startTime", "finishTime", "tasks"})
public class Workflow {
	private final String m_name;
	private final String m_modelId;
	private final WorkflowStatus m_status;
	private final LocalDateTime m_creationTime;
	private final LocalDateTime m_startTime;
	private final LocalDateTime m_finishTime;
	private final List<NodeTask> m_tasks;
	
	public Workflow(@JsonProperty("name") String name,
					@JsonProperty("modelId") String modelId,
					@JsonProperty("status") WorkflowStatus status,
					@JsonProperty("creationTime") LocalDateTime creationTime,
					@JsonProperty("startTime") LocalDateTime startTime,
					@JsonProperty("finishTime") LocalDateTime finishTime,
					@JsonProperty("tasks") List<NodeTask> tasks) {
        m_name = name;
        m_modelId = modelId;
        m_status = status;
        m_creationTime = creationTime;
        m_startTime = startTime;
        m_finishTime = finishTime;
        m_tasks = sortTopologically(tasks);
    }
	
	public Workflow(Builder builder) {
		Preconditions.checkArgument(builder != null, "DefaultWorkflow.Builder is null");
		Preconditions.checkArgument(builder.m_name != null, "Name is null");
		
		m_name = builder.m_name;
		m_modelId = builder.m_modelId;
		m_status = builder.m_status;
		m_creationTime = builder.m_creationTime;
		m_startTime = builder.m_startTime;
		m_finishTime = builder.m_finishTime;
		m_tasks = sortTopologically(builder.m_tasks);
	}

	public String getName() {
		return m_name;
	}

	public String getModelId() {
		return m_modelId;
	}
	
	public WorkflowStatus getStatus() {
		return m_status;
	}
	
	public LocalDateTime getCreationTime() {
		return m_creationTime;
	}

	public LocalDateTime getStartTime() {
		return m_startTime;
	}

	public LocalDateTime getFinishTime() {
		return m_finishTime;
	}

	public List<NodeTask> getTasks() {
		return m_tasks;
	}
	
	@Override
	public String toString() {
		String nodesStatusStr = FStream.from(m_tasks)
										.map(nt -> String.format("%s(%s)", nt.getTaskId(), nt.getStatus()))
										.join(", ");
		return String.format("Workflow[name=%s, model=%s, status=%s, nodes={%s}]",
							m_name, m_modelId, m_status, nodesStatusStr);
	}
	
	private static List<NodeTask> sortTopologically(List<NodeTask> nodeTasks) {
		List<NodeTask> remains = Lists.newArrayList(nodeTasks);
		List<NodeTask> sorted = Lists.newArrayList();
		Set<String> sortedNames = FStream.from(sorted)
                                            .map(NodeTask::getTaskId)
                                            .toSet();
		
		while ( remains.size() > 0 ) {
			NodeTask task = remains.remove(0);
			
			if ( FStream.from(task.getDependents())
						.exists(t -> !sortedNames.contains(t)) ) {
				remains.add(task);
			}
			else {
				sorted.add(task);
				sortedNames.add(task.getTaskId());
			}
		}
		
		return sorted;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String m_name;
		private String m_modelId;
		private WorkflowStatus m_status;
		private LocalDateTime m_creationTime;
		private LocalDateTime m_startTime;
		private LocalDateTime m_finishTime;
		private final List<NodeTask> m_tasks = Lists.newArrayList();
		
		private Builder() { }
		
		public Workflow build() {
			return new Workflow(this);
		}
		
		public Builder name(String name) {
			m_name = name;
			
			if ( m_modelId == null ) {
				m_modelId = Utilities.splitLast(name, '-')._1;
			}
			
			return this;
		}
		
		public Builder modelId(String id) {
			m_modelId = id;
			return this;
		}
		
		public Builder status(WorkflowStatus status) {
			m_status = status;
			return this;
		}
		
		public Builder creationTime(LocalDateTime ldt) {
			m_creationTime = ldt;
			return this;
		}
		
		public Builder startTime(LocalDateTime ldt) {
			m_startTime = ldt;
			return this;
		}
		
		public Builder finishTime(LocalDateTime ldt) {
			m_finishTime = ldt;
			return this;
		}
		
		public Builder tasks(List<NodeTask> tasks) {
			m_tasks.addAll(tasks);
			return this;
		}
	}
}
