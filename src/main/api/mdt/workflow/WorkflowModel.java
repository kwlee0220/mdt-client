package mdt.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.checkerframework.com.google.common.base.Preconditions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.InternalException;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix = "m_")
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "description", "taskDescriptors", "gui"})
public class WorkflowModel {
	private String m_id;
	private @Nullable String m_name;
	private @Nullable String m_description;

	private List<TaskDescriptor> m_taskDescriptors = Lists.newArrayList();
	private Map<String, Object> m_gui = Maps.newHashMap();
	
	public void addTaskDescriptor(TaskDescriptor taskDesc) {
		Preconditions.checkArgument(taskDesc != null, "taskDesc must not be null");
		
		if ( m_taskDescriptors.contains(taskDesc) ) {
			throw new IllegalArgumentException("taskDesc already exists: " + taskDesc.getId());
		}
		
		m_taskDescriptors.add(taskDesc);
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
	
	private static List<TaskDescriptor> sortTopologically(Collection<TaskDescriptor> tasks) {
		List<TaskDescriptor> remains = Lists.newArrayList(tasks);
		List<TaskDescriptor> sorted = Lists.newArrayList();
		Set<String> sortedNames = FStream.from(sorted)
                                            .map(TaskDescriptor::getId)
                                            .toSet();
		
		while ( remains.size() > 0 ) {
			TaskDescriptor taskDesc = remains.remove(0);
			
			if ( FStream.from(taskDesc.getDependencies())
						.exists(t -> !sortedNames.contains(t)) ) {
				remains.add(taskDesc);
			}
			else {
				sorted.add(taskDesc);
				sortedNames.add(taskDesc.getId());
			}
		}
		
		return sorted;
	}
}
