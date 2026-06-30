package mdt.workflow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.Preconditions;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "description", "taskDescriptors", "gui"})
public class WorkflowModel {
	private String m_id;
	private @Nullable String m_name;
	private @Nullable String m_description;

	private List<TaskDescriptor> m_taskDescriptors = Lists.newArrayList();
	private Map<String, Object> m_gui = Maps.newHashMap();
	
	public void addTaskDescriptor(TaskDescriptor taskDesc) {
		Preconditions.checkNotNullArgument(taskDesc, "TaskDescriptor must not be null");
		
		if ( m_taskDescriptors.contains(taskDesc) ) {
			throw new IllegalArgumentException("taskDesc already exists: " + taskDesc.getId());
		}
		
		m_taskDescriptors.add(taskDesc);
	}
	
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	public @Nullable String getName() {
		return m_name;
	}
	
	public void setName(@Nullable String name) {
		m_name = name;
	}
	
	public @Nullable String getDescription() {
		return m_description;
	}
	
	public void setDescription(@Nullable String description) {
		m_description = description;
	}
	
	public List<TaskDescriptor> getTaskDescriptors() {
		return m_taskDescriptors;
	}
	
	public void setTaskDescriptors(List<TaskDescriptor> taskDescs) {
		m_taskDescriptors = taskDescs;
	}
	
	public Map<String, Object> getGui() {
		return m_gui;
	}
	
	public void setGui(Map<String, Object> gui) {
		m_gui = gui;
	}
	
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}
	
	public static WorkflowModel parseJsonFile(File workflowDescFile)
		throws StreamReadException, DatabindException, IOException {
		return MDTModelSerDe.getJsonMapper().readValue(workflowDescFile, WorkflowModel.class);
	}
	
	public static WorkflowModel parseJsonString(String workflowDescJson)
		throws JsonProcessingException {
		return MDTModelSerDe.getJsonMapper()
							.readValue(workflowDescJson, WorkflowModel.class);
	}
	
	public static InternalException toInternalException(String jsonStr, Exception cause) {
		String msg = String.format("Failed parse WorkflowDescriptor: json=%s, cause=%s",
									jsonStr, cause);
		return new InternalException(msg);
	}
	
	@Override
	public String toString() {
		return String.format("WorkflowModel(id=%s, name=%s, #tasks=%d)",
								m_id, m_name, m_taskDescriptors.size());
	}
}
