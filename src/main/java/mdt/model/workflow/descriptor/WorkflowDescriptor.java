package mdt.model.workflow.descriptor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import utils.InternalException;
import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.AASUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class WorkflowDescriptor {
	private String m_id;
	@Nullable private String m_name;
	@Nullable private String m_description;

	private KeyedValueList<String,ParameterDescriptor> m_parameters;
	private KeyedValueList<String,TaskDescriptor> m_tasks = new KeyedValueList<>(TaskDescriptor::getId);
	private BuiltInAwareTaskTemplateList m_taskTemplates = new BuiltInAwareTaskTemplateList();
	
	public WorkflowDescriptor() { }
	
	public String getId() {
		return m_id;
	}
	public void setId(String id) {
		m_id = id;
	}

	public String getName() {
		return m_name;
	}
	
	public void setName(String name) {
		m_name = name;
	}
	
	public String getDescription() {
		return m_description;
	}
	public void setDescription(String desc) {
		m_description = desc;
	}

	public KeyedValueList<String,ParameterDescriptor> getParameters() {
		if ( m_parameters == null ) {
			m_parameters = new KeyedValueList<>(ParameterDescriptor::getName);
		}
		
		return m_parameters;
	}
	@JsonProperty("parameters")
	public KeyedValueList<String,ParameterDescriptor> getParametersForJackson() {
		return m_parameters.size() > 0 ? m_parameters : null;
	}
	public void setParameters(Collection<ParameterDescriptor> parameters) {
		m_parameters = (parameters != null) ? KeyedValueList.from(parameters, ParameterDescriptor::getName)
											: new KeyedValueList<>(ParameterDescriptor::getName);
	}
	
	public KeyedValueList<String,TaskDescriptor> getTasks() {
		return m_tasks;
	}
	public void setTasks(List<TaskDescriptor> tasks) {
		m_tasks.clear();
		if ( tasks != null ) {
			FStream.from(tasks).forEach(m_tasks::add);
		}
	}
	
	public BuiltInAwareTaskTemplateList getTaskTemplates() {
		return m_taskTemplates;
	}

	@JsonProperty("taskTemplates")
	public List<TaskTemplateDescriptor> getUserDefinedTaskTemplateAll() {
		return m_taskTemplates.getUserDefinedTaskTemplateAll();
	}
	
	@JsonProperty("taskTemplates")
	public void setUserDefinedTaskTemplates(List<TaskTemplateDescriptor> userTaskTemplates) {
		m_taskTemplates = new BuiltInAwareTaskTemplateList();
		if ( userTaskTemplates != null ) {
			FStream.from(userTaskTemplates).forEach(m_taskTemplates::add);
		}
	}
	
	public boolean isBuiltInTaskTemplate(String tmpltId) {
		return m_taskTemplates.isBuiltInTaskTemplate(tmpltId);
	}
	
	public String toJsonString() {
		return AASUtils.writeJson(this);
	}
	
	public static WorkflowDescriptor parseJsonFile(File workflowDescFile)
		throws StreamReadException, DatabindException, IOException {
		return AASUtils.getJsonMapper().readValue(workflowDescFile, WorkflowDescriptor.class);
	}
	
	public static WorkflowDescriptor parseJsonString(String workflowDescJson)
		throws JsonProcessingException {
		return AASUtils.getJsonMapper()
						.readValue(workflowDescJson, WorkflowDescriptor.class);
	}
	
	public static InternalException toInternalException(String jsonStr, Exception cause) {
		String msg = String.format("Failed parse WorkflowDescriptor: json=%s, cause=%s",
									jsonStr, cause);
		return new InternalException(msg);
	}
}
