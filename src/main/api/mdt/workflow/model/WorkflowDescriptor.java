package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import lombok.Getter;
import lombok.Setter;

import utils.InternalException;
import utils.KeyedValueList;

import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonInclude(Include.NON_NULL)
public class WorkflowDescriptor {
	private String id;
	@Nullable private String name;
	@Nullable private String description;

	private KeyedValueList<String,TaskDescriptor> tasks = KeyedValueList.newInstance(TaskDescriptor::getId);

	public void setTasks(Collection<TaskDescriptor> tasks) {
		this.tasks = KeyedValueList.from(tasks, TaskDescriptor::getId);
	}
	
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}
	
	public static WorkflowDescriptor parseJsonFile(File workflowDescFile)
		throws StreamReadException, DatabindException, IOException {
		return MDTModelSerDe.getJsonMapper().readValue(workflowDescFile, WorkflowDescriptor.class);
	}
	
	public static WorkflowDescriptor parseJsonString(String workflowDescJson)
		throws JsonProcessingException {
		return MDTModelSerDe.getJsonMapper()
						.readValue(workflowDescJson, WorkflowDescriptor.class);
	}
	
	public static InternalException toInternalException(String jsonStr, Exception cause) {
		String msg = String.format("Failed parse WorkflowDescriptor: json=%s, cause=%s",
									jsonStr, cause);
		return new InternalException(msg);
	}
}
