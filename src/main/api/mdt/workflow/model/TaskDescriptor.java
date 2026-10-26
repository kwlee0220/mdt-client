package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import utils.InternalException;
import utils.Preconditions;
import utils.func.FOption;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "name", "type", "description", "dependencies", "triggerRule",
					"inputArguments", "outputArguments", "options", "labels"})
@JsonIncludeProperties({ "id", "name", "type", "description", "dependencies", "triggerRule",
					"inputArguments", "outputArguments", "options", "labels" })
@JsonInclude(Include.NON_NULL)
public final class TaskDescriptor {
	public static final String OPTION_OPERATION = "operation";
	public static final String OPTION_OPERATION_SUBMODEL = "operationSubmodel";
	public static final String OPTION_TIMEOUT = "timeout";	// optional
	public static final String OPTION_POLL_INTERVAL = "poll";    // optional
	public static final String OPTION_LOG_LEVEL = "loglevel";
	public static final String LABEL_MDT_OPERATION = "mdt-operation";
	
	private String m_id;
	private @Nullable String m_name;
	private String m_type;
	private @Nullable String m_description;
	
	private Set<String> m_dependencies = new HashSet<>();
	private TriggerRule m_triggerRule = TriggerRule.ALL_SUCCESS;
	private LinkedHashMap<String,ArgumentSpec> m_inputArgumentSpecs = new LinkedHashMap<>();
	private LinkedHashMap<String,ArgumentSpec.ReferenceArgument> m_outputArgumentSpecs = new LinkedHashMap<>();
	private Map<String, Option> m_options = new LinkedHashMap<>();
	private List<NameValue> m_labels = new ArrayList<>();
	
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
		Preconditions.checkNotNullArgument(id, "id must not be null");

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
		Preconditions.checkNotNullArgument(type, "type must not be null");

		m_type = type;
	}
	
	public String getDescription() {
		return m_description;
	}
	
	public void setDescription(String description) {
		m_description = description;
	}
	
	public Set<String> getDependencies() {
		return Collections.unmodifiableSet(m_dependencies);
	}
	
	public void addDependency(String... dependency) {
		Preconditions.checkNotNullArgument(dependency, "dependency must not be null");

		FStream.of(dependency).forEach(m_dependencies::add);
	}
	
	public void setDependencies(Iterable<String> dependencies) {
		Preconditions.checkNotNullArgument(dependencies, "dependencies must not be null");

		m_dependencies = Sets.newHashSet(dependencies);
	}

	/**
	 * 직전 task들의 종료 상태에 따라 이 task를 실행할지 정하는 규칙을 반환한다.
	 *
	 * @return	trigger rule. 지정하지 않았으면 {@link TriggerRule#ALL_SUCCESS}.
	 */
	@NotNull
	public TriggerRule getTriggerRule() {
		return m_triggerRule;
	}

	public void setTriggerRule(TriggerRule rule) {
		Preconditions.checkNotNullArgument(rule, "trigger rule must not be null");

		m_triggerRule = rule;
	}

	// 기본값은 JSON에 기록하지 않아 기존 워크플로 JSON과 동일한 결과를 유지한다.
	@JsonProperty("triggerRule")
	private @Nullable TriggerRule getTriggerRuleForJackson() {
		return (m_triggerRule != TriggerRule.ALL_SUCCESS) ? m_triggerRule : null;
	}

	// 생략하거나 null이면 기본값을 쓴다.
	@JsonProperty("triggerRule")
	private void setTriggerRuleForJackson(@Nullable TriggerRule rule) {
		m_triggerRule = (rule != null) ? rule : TriggerRule.ALL_SUCCESS;
	}

	public ArgumentSpec getInputArgument(String argId) {
		return m_inputArgumentSpecs.get(argId);
	}
	public void addInputArgument(ArgumentSpec spec) {
		Preconditions.checkNotNullArgument(spec, "Input argument spec must not be null");
		if ( m_inputArgumentSpecs.containsKey(spec.getId()) ) {
			throw new IllegalArgumentException("Input argument spec already exists: " + spec.getId());
		}
		m_inputArgumentSpecs.put(spec.getId(), spec);
	}
	
	@JsonProperty("inputArguments")
	public List<ArgumentSpec> getInputArguments() {
		return Lists.newArrayList(m_inputArgumentSpecs.values());
	}

	@JsonProperty("inputArguments")
	public void setInputArguments(Iterable<ArgumentSpec> vars) {
		m_inputArgumentSpecs = FStream.from(vars)
										.tagKey(ArgumentSpec::getId)
										.toMap(new LinkedHashMap<>());
	}
	
	public ArgumentSpec.ReferenceArgument getOutputArgument(String argId) {
		return m_outputArgumentSpecs.get(argId);
	}
	public void addOutputArgument(ArgumentSpec.ReferenceArgument spec) {
		Preconditions.checkNotNullArgument(spec, "Output argument spec must not be null");
		if ( m_outputArgumentSpecs.containsKey(spec.getId()) ) {
			throw new IllegalArgumentException("Output argument spec already exists: " + spec.getId());
		}
		m_outputArgumentSpecs.put(spec.getId(), spec);
	}
	
	@JsonProperty("outputArguments")
	public List<ArgumentSpec.ReferenceArgument> getOutputArguments() {
		return Lists.newArrayList(m_outputArgumentSpecs.values());
	}

	@JsonProperty("outputArguments")
	public void setOutputVariables(Iterable<ArgumentSpec.ReferenceArgument> args) {
		m_outputArgumentSpecs = FStream.from(args)
										.tagKey(ArgumentSpec::getId)
										.toMap(new LinkedHashMap<>());
	}
	
	@NotNull
	public Map<String,Option> getOptions() {
		return m_options;
	}
	
	public FOption<String> findOptionValue(String optName) {
		return FOption.ofNullable(m_options.get(optName))
						.map(Option::getValue);
	}
	
	public void addOption(String name, String value) {
		m_options.put(name, new Option(name, value));
	}
	
	@JsonProperty("options")
	public Collection<Option> getOptionsForJackson() {
		return m_options.values();
	}
	
	@JsonProperty("options")
	public void setOptionsForJackson(Collection<Option> options) {
		Preconditions.checkNotNullArgument(options, "options must not be null");
		
		m_options = FStream.from(options)
							.tagKey(Option::getName)
							.toMap(new LinkedHashMap<>());
	}
	
	public List<NameValue> getLabels() {
		return Collections.unmodifiableList(m_labels);
	}
	public FOption<String> findLabel(String name) {
		return FStream.from(m_labels)
						.findFirst(lb -> lb.getName().equals(name))
						.map(NameValue::getValue);
	}

	public void addLabel(String name, String value) {
		Preconditions.checkNotNullArgument(name, "label name must not be null");
		Preconditions.checkNotNullArgument(value, "label value must not be null");

		m_labels.add(NameValue.of(name, value));
	}

	public void setLabels(Iterable<NameValue> labels) {
		Preconditions.checkNotNullArgument(labels, "labels must not be null");
		
		m_labels = Lists.newArrayList(labels);
	}

	public static TaskDescriptor parseJsonString(String json) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(json, TaskDescriptor.class);
	}
	public static TaskDescriptor parseJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile, TaskDescriptor.class);
	}
	
	public static List<TaskDescriptor> parseListJsonFile(File jsonFile) throws IOException {
		return MDTModelSerDe.getJsonMapper().readValue(jsonFile,
													new TypeReference<List<TaskDescriptor>>() {});
	}
	
	public String toSignatureString() {
		String taskTypeId = TaskDescriptors.toShortTaskTypeId(m_type);
		String inArgNames = FStream.from(m_inputArgumentSpecs.keySet()).join(", ");
		String outArgNames = FStream.from(m_outputArgumentSpecs.keySet()).join(", ");
		return String.format("%s[%s]: (%s) -> (%s)", taskTypeId, getId(), inArgNames, outArgNames);
	}
	
	public String toJsonString() {
		try {
			return MDTModelSerDe.getJsonMapper()
								.writerFor(TaskDescriptor.class)
								.writeValueAsString(this);
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to serialize TaskDescriptor: %s", this);
			throw new InternalException(msg, e);
		}
	}
	private static final int CHUNK_SIZE = 200;
	public List<String> toEncodedString() {
		byte[] bytes = toJsonString().getBytes(StandardCharsets.UTF_8);
		String fullStr = IOUtils.stringify(bytes);
		
	    int length = fullStr.length();
	    List<String> chunks = new ArrayList<>();
	    for (int i = 0; i < length; i += CHUNK_SIZE) {
	        int endIndex = Math.min(i + CHUNK_SIZE, length);
	        chunks.add(fullStr.substring(i, endIndex));
	    }
	    
	    return chunks;
	}
	
	public static TaskDescriptor parseEncodedString(List<String> chunks) throws IOException {
		try {
			String encodedStr = FStream.from(chunks)
										.fold(new StringBuilder(), StringBuilder::append)
										.toString();

			byte[] bytes = IOUtils.destringify(encodedStr);
			String jsonStr = new String(bytes, StandardCharsets.UTF_8);
			return parseJsonString(jsonStr);
		}
		catch ( IOException e ) {
			// IOException는 그대로 던진다.(InternalException로 wrapping하지 않도록 함)
			throw e;
		}
		catch ( Exception e ) {
			throw new InternalException("Failed to parse encoded TaskDescriptor", e);
		}
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
			case "mdt.task.SetTask" -> "Set";
//			case "mdt.task.CopyTask" -> "Copy";
			case "mdt.task.ProgramTask" -> "Program";
			case "mdt.task.HttpTask" -> "Http";
			case "mdt.task.AASOperationTask" -> "AAS";
			default -> TaskDescriptors.toShortTaskTypeId(m_type);
		};

//		String optList = FStream.from(m_options)
//								.flatMapIterable(Option::toCommandOptionSpec)
//								.join(", ", "[", "]");
		return String.format("%s: %s, opts:%s", taskName, toSignatureString(), m_options);
	}
}
