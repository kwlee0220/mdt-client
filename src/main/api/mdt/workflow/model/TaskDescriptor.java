package mdt.workflow.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import utils.InternalException;
import utils.KeyValue;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Optionals;
import utils.io.IOUtils;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.MDTModelSerDe;
import mdt.model.NameValue;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.SubmodelBasedElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;
import mdt.model.sm.variable.Variable;
import mdt.workflow.model.ArgumentSpec.LiteralArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "name", "type", "submodel", "description", "dependencies",
					"inputVariables", "outputVariables", "options", "labels"})
@JsonIncludeProperties({ "id", "name", "type", "submodel", "description", "dependencies",
					"inputVariables", "outputVariables", "options", "labels" })
@JsonInclude(Include.NON_NULL)
public final class TaskDescriptor {
	private String m_id;
	private @Nullable String m_name;
	private String m_type;
	private @Nullable String m_description;
	
	private DefaultSubmodelReference m_submodelRef;
	
	private Set<String> m_dependencies = Sets.newHashSet();
	private LinkedHashMap<String,ArgumentSpec> m_inputArgumentSpecs = Maps.newLinkedHashMap();
	private LinkedHashMap<String,ReferenceArgumentSpec> m_outputArgumentSpecs = Maps.newLinkedHashMap();
	private Map<String, Option> m_options = Maps.newHashMap();
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
	
	@JsonProperty("submodel")
	public String getSubmodelRefString() {
		return Optionals.map(m_submodelRef, DefaultSubmodelReference::toStringExpr);
	}

	@JsonProperty("submodel")
	public void setSubmodelRefString(String refString) {
		m_submodelRef = ElementReferences.parseSubmodelReference(refString);
	}
	
	public DefaultSubmodelReference getSubmodelRef() {
		return m_submodelRef;
	}
	
	public void setSubmodelRef(DefaultSubmodelReference submodelRef) {
		m_submodelRef = submodelRef;
	}
	
	public Set<String> getDependencies() {
		return m_dependencies;
	}
	
	public void addDependency(String... dependency) {
		Preconditions.checkArgument(dependency != null, "dependency must not be null");

		FStream.of(dependency).forEach(m_dependencies::add);
	}
	
	public void setDependencies(Iterable<String> dependencies) {
		Preconditions.checkArgument(dependencies != null, "dependencies must not be null");

		m_dependencies = Sets.newHashSet(dependencies);
	}
	
	public LinkedHashMap<String,ArgumentSpec> getInputArgumentSpecs() {
		return m_inputArgumentSpecs;
	}
	public void addInputArgumentSpec(String argId, ArgumentSpec spec) {
		Preconditions.checkArgument(spec != null, "Input argument spec must not be null");
		m_inputArgumentSpecs.put(argId, spec);
	}
	
	@JsonProperty("inputVariables")
	public List<Variable> getInputVariables() {
		return KeyValueFStream.from(m_inputArgumentSpecs)
								.map(this::toVariable)
								.toList();
	}

	@JsonProperty("inputVariables")
	public void setInputVariables(Iterable<Variable> vars) {
		m_inputArgumentSpecs = FStream.from(vars)
										.mapToKeyValue(this::fromArgumentSpec)
										.toMap(new LinkedHashMap<>());
	}
	
	public LinkedHashMap<String,ReferenceArgumentSpec> getOutputArgumentSpecs() {
		return m_outputArgumentSpecs;
	}
	public void addOutputArgumentSpec(String argId, ReferenceArgumentSpec spec) {
		Preconditions.checkArgument(spec != null, "Output argument spec must not be null");

		m_outputArgumentSpecs.put(argId, spec);
	}
	
	@JsonProperty("outputVariables")
	public List<Variable> getOutputVariables() {
		return KeyValueFStream.from(m_outputArgumentSpecs)
								.map(this::toVariable)
								.toList();
	}

	@JsonProperty("outputVariables")
	public void setOutputVariables(Iterable<Variable> vars) {
		m_outputArgumentSpecs = Maps.newLinkedHashMap();
		for ( Variable var : vars ) {
			Preconditions.checkArgument(var instanceof ReferenceVariable,
										"Output variable must be ReferenceVariable: %s", var);
			
			MDTElementReference ref = (MDTElementReference)((ReferenceVariable)var).getReference();
			ReferenceArgumentSpec argSpec = ArgumentSpec.reference(ref);
			m_outputArgumentSpecs.put(var.getName(), argSpec);
		}
	}
	
	public Map<String,Option> getOptions() {
		return m_options;
	}
	
	public Optional<String> findOptionValue(String optName) {
		return Optional.ofNullable(m_options.get(optName)).map(Option::getValue);
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
		Preconditions.checkArgument(options != null, "options must not be null");
		
		m_options = FStream.from(options)
							.tagKey(Option::getName)
							.toMap();
		
		// 만일 optiona 중에 operation이 포함되어 있고, submodelRef가 비어있다면
		// operation 값을 이용해서 submodelRef를 설정한다.
		if ( m_submodelRef == null ) {
			Funcs.findFirst(options, opt -> opt.getName().equals("operation"))
					.map(Option::getValue)
					.ifPresent(opStr -> {
						try {
							SubmodelBasedElementReference opRef = (SubmodelBasedElementReference)ElementReferences.parseExpr(opStr);
							m_submodelRef = (DefaultSubmodelReference)opRef.getSubmodelReference();
						}
						catch ( Throwable ignored ) { }
					});
		}
		
	}
	
	public List<NameValue> getLabels() {
		return m_labels;
	}
	public FOption<String> findLabel(String name) {
		return FStream.from(m_labels)
						.findFirst(lb -> lb.getName().equals(name))
						.map(NameValue::getValue);
	}

	public void addLabel(String name, String value) {
		Preconditions.checkArgument(name != null, "label name must not be null");
		Preconditions.checkArgument(value != null, "label value must not be null");

		m_labels.add(NameValue.of(name, value));
	}

	public void setLabels(Iterable<NameValue> labels) {
		Preconditions.checkArgument(labels != null, "labels must not be null");
		
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
			return MDTModelSerDe.MAPPER
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
	    List<String> chunks = Lists.newArrayList();
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
		catch ( Throwable e ) {
			e.printStackTrace();
			return null;
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
		return String.format("%s: %s, opts:%s", taskName, toSignatureString(), m_options);
	}
	
	public void updateOutputArguments(MDTInstanceManager manager, Map<String,SubmodelElement> outArgs,
										Logger logger) {
		Map<String,ElementValue> outValues = KeyValueFStream.from(outArgs)
															.mapValue(ElementValues::getValue)
															.toMap();
		updateOutputArgumentsWithValues(manager, outValues, logger);
	}
	
	public void updateOutputArgumentsWithValues(MDTInstanceManager manager, Map<String,ElementValue> outValues,
												Logger logger) {
		for ( Map.Entry<String,ElementValue> ent: outValues.entrySet() ) {
			String argId = ent.getKey();
			ElementValue argValue = ent.getValue();
					
			// Output argument와 동일 이름을 가진 variable을 찾는다.
			ArgumentSpec outArgSpec = m_outputArgumentSpecs.get(argId);
			if ( outArgSpec == null ) {
				logger.warn("Unknown output argument: id={}", argId);
				continue;
			}
			if ( outArgSpec instanceof ReferenceArgumentSpec refArgSpec ) {
				try {
					refArgSpec.activate(manager);
					refArgSpec.updateValue(argValue);
					logger.info("Updated: output variable {}({}): {}",
								argId, refArgSpec.getReferenceString(), argValue);
				}
				catch ( IOException e ) {
					logger.error("Failed to update output variable[{}], cause={}", argId, e);
				}
            }
		}
	}
	private Variable toVariable(String argId, ArgumentSpec argSpec) {
		if ( argSpec instanceof ReferenceArgumentSpec refArgSpec ) {
			return new ReferenceVariable(argId, "", refArgSpec.getElementReference());
		}
		else if ( argSpec instanceof LiteralArgumentSpec litArgSpec ) {
			return new ValueVariable(argId, "", litArgSpec.readValue());
		}
		else {
			throw new IllegalArgumentException("Unsupported argument spec type: " + argSpec);
		}
	}
	private KeyValue<String,ArgumentSpec> fromArgumentSpec(Variable var) {
		if ( var instanceof ReferenceVariable refVar ) {
			ReferenceArgumentSpec argSpec = ArgumentSpec.reference((MDTElementReference)refVar.getReference());
			return KeyValue.of(var.getName(), argSpec);
		}
		else if ( var instanceof ValueVariable valVar ) {
			LiteralArgumentSpec argSpec = ArgumentSpec.literal(valVar.readValue());
			return KeyValue.of(var.getName(), argSpec);
		}
		else {
			throw new IllegalArgumentException("Unsupported variable type: " + var);
		}
	}
}
