package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonPropertyOrder({"id", "name", "type", "description", "dependencies", "variables", "options", "labels"})
@JsonInclude(Include.NON_NULL)
public class TaskDescriptor {
	private String id;
	@Nullable private String name;
	private String type;
	@Nullable private String description;
	
	private Set<String> dependencies = Sets.newHashSet();
	private KeyedValueList<String,VariableDescriptor> variables = KeyedValueList.newInstance(VariableDescriptor::getName);
	private KeyedValueList<String,Option> options = KeyedValueList.newInstance(Option::getName);
	private KeyedValueList<String,NameValue> labels = KeyedValueList.newInstance(NameValue::getName);

	public void setVariables(Collection<VariableDescriptor> variables) {
		this.variables = (variables != null)
						? KeyedValueList.from(variables, VariableDescriptor::getName)
						: KeyedValueList.newInstance(VariableDescriptor::getName);
	}

	public void setOptions(Collection<Option> options) {
		this.options = (options != null)
						? KeyedValueList.from(options, Option::getName)
						: KeyedValueList.newInstance(Option::getName);
	}

	public void setLabels(Collection<NameValue> labels) {
		this.labels = (options != null)
					? KeyedValueList.from(labels, NameValue::getName)
					: KeyedValueList.newInstance(NameValue::getName);
	}
	
	public static TaskDescriptor parseJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile, TaskDescriptor.class);
	}
	
	public static List<TaskDescriptor> parseListJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile,
													new TypeReference<List<TaskDescriptor>>() {});
	}
	
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		TaskDescriptor other = (TaskDescriptor)obj;
		return Objects.equal(id, other.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
	
	@Override
	public String toString() {
		String taskName = switch ( this.type ) {
			case "mdt.task.builtin.SetTask" -> "Set";
			case "mdt.task.builtin.CopyTask" -> "Copy";
			case "mdt.task.builtin.ProgramTask" -> "Program";
			case "mdt.task.builtin.HttpTask" -> "Http";
			case "mdt.task.builtin.AASOperationTask" -> "AAS";
			case "mdt.task.builtin.JsltTask" -> "Jslt";
			default -> throw new AssertionError();
		};
		
		String varList = FStream.from(this.variables)
								.map(v -> String.format("%s(%s)", v.getName(), v.getKind().toString().charAt(0)))
								.join(", ");
		String optList = FStream.from(this.options)
								.flatMapIterable(Option::toCommandOptionSpec)
								.join(", ");
		return String.format("%s(%s: vars=%s, opts=%s)", taskName, id, varList, optList);
	}
}
