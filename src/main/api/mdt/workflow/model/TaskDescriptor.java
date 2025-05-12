package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.KeyedValueList;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.variable.Variable;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "name", "type", "description", "dependencies", "inputVariables", "outputVariables",
					"options", "labels"})
@JsonInclude(Include.NON_NULL)
public final class TaskDescriptor {
	private String m_id;
	@Nullable private String m_name;
	private String m_type;
	@Nullable private String m_description;
	
	private Set<String> m_dependencies = Sets.newHashSet();
	private KeyedValueList<String,Variable> m_inputVariables = KeyedValueList.with(Variable::getName);
	private KeyedValueList<String,Variable> m_outputVariables = KeyedValueList.with(Variable::getName);
	private KeyedValueList<String, Option<?>> m_options = KeyedValueList.with(Option::getName);
	private List<NameValue> m_labels = Lists.newArrayList();
	
	public TaskDescriptor() { }
	
	public TaskDescriptor(String id, String name, String type) {
		m_id = id;
		m_name = name;
		m_type = type;
	}
	
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		Preconditions.checkArgument(id != null, "id must not be null");

		m_id = id;
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String name) {
		m_name = name;
	}
	
	public String getType() {
		return m_type;
	}
	
	public void setType(String type) {
		Preconditions.checkArgument(type != null, "type must not be null");

		m_type = type;
	}
	
	public String getDescription() {
		return m_description;
	}
	
	public void setDescription(String description) {
		m_description = description;
	}
	
	public Set<String> getDependencies() {
		return m_dependencies;
	}
	
	public void setDependencies(Iterable<String> dependencies) {
		Preconditions.checkArgument(dependencies != null, "dependencies must not be null");

		m_dependencies = Sets.newHashSet(dependencies);
	}
	
	public KeyedValueList<String,Variable> getInputVariables() {
		return m_inputVariables;
	}

	public void setInputVariables(List<Variable> variables) {
		Preconditions.checkArgument(variables != null, "Input variables must not be null");

		m_inputVariables = KeyedValueList.from(variables, Variable::getName);
	}
	
	public KeyedValueList<String,Variable> getOutputVariables() {
		return m_outputVariables;
	}

	public void setOutputVariables(List<Variable> variables) {
		Preconditions.checkArgument(variables != null, "Output variables must not be null");

		m_outputVariables = KeyedValueList.from(variables, Variable::getName);
	}
	
	public KeyedValueList<String,Option<?>> getOptions() {
		return m_options;
	}

	public void setOptions(Iterable<Option<?>> options) {
		Preconditions.checkArgument(options != null, "options must not be null");
		
		m_options = KeyedValueList.from(options, Option::getName);
	}
	
	public boolean addOption(String name, String value) {
		return m_options.add(new StringOption(name, value));
	}
	public boolean addOption(String name, boolean flag) {
		return m_options.add(new BooleanOption(name, flag));
	}
	public boolean addOption(String name, Duration dur) {
		return m_options.add(new DurationOption(name, dur));
	}
	public boolean addOption(String name, File file) {
		return m_options.add(new FileOption(name, file));
	}
	public boolean addOption(String name, MDTElementReference ref) {
		return m_options.add(new MDTElementRefOption(name, ref));
	}
	public boolean addOption(String name, MDTSubmodelReference ref) {
		return m_options.add(new MDTSubmodelRefOption(name, ref));
	}
	
	public List<NameValue> getLabels() {
		return m_labels;
	}

	public void setLabels(Iterable<NameValue> labels) {
		Preconditions.checkArgument(labels != null, "labels must not be null");
		
		m_labels = Lists.newArrayList(labels);
	}
	
	public FOption<Option<?>> findOption(String optName) {
		return FStream.from(m_options)
						.findFirst(opt -> opt.getName().equals(optName));
	}
	
	public <T extends Option<?>> FOption<T> findOption(String optName, Class<T> optionType) {
		return findOption(optName)
						.mapOrThrow(opt -> {
							if ( optionType.isInstance(opt) ) {
								return optionType.cast(opt);
							}
							
							String details = String.format("Invalid option type: %s (expected type: %s)",
															opt, optionType);
							throw new IllegalArgumentException(details);
						});
	}
	
	public static TaskDescriptor parseJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile, TaskDescriptor.class);
	}
	
	public static List<TaskDescriptor> parseListJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile,
													new TypeReference<List<TaskDescriptor>>() {});
	}
	
	public String toSignatureString() {
		String inPortNames = FStream.from(m_inputVariables).map(Variable::getName).join(", ");
		String outPortNames = FStream.from(m_outputVariables).map(Variable::getName).join(", ");
		return String.format("%s(%s) -> (%s)", getId(), inPortNames, outPortNames);
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
		return Objects.equal(m_id, other.m_id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(m_id);
	}
	
	@Override
	public String toString() {
		String taskName = switch ( m_type ) {
			case "mdt.task.builtin.SetTask" -> "Set";
//			case "mdt.task.builtin.CopyTask" -> "Copy";
			case "mdt.task.builtin.ProgramTask" -> "Program";
			case "mdt.task.builtin.HttpTask" -> "Http";
			case "mdt.task.builtin.AASOperationTask" -> "AAS";
//			case "mdt.task.builtin.JsltTask" -> "Jslt";
			default -> throw new AssertionError();
		};

//		String optList = FStream.from(m_options)
//								.flatMapIterable(Option::toCommandOptionSpec)
//								.join(", ", "[", "]");
		return String.format("%s: %s, opts=%s", taskName, toSignatureString(), m_options);
	}
}
