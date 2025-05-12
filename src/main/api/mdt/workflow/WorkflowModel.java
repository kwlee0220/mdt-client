package mdt.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import utils.InternalException;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonInclude(Include.NON_NULL)
public class WorkflowModel {
	private String id;
	@Nullable private String name;
	@Nullable private String description;

	private List<TaskDescriptor> taskDescriptors = Lists.newArrayList();

	public void setTasks(Collection<TaskDescriptor> tasks) {
		this.taskDescriptors = sortTopologically(tasks);
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
