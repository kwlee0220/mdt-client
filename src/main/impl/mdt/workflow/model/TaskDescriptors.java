package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;

import lombok.experimental.UtilityClass;

import utils.KeyedValueList;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.ModelValidationException;
import mdt.model.NameValue;
import mdt.model.Qualifiers;
import mdt.model.ReferenceUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.ProgramOperationDescriptor;
import mdt.task.builtin.SetTask;
import mdt.task.builtin.TaskUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class TaskDescriptors {
	public static TaskDescriptor newSetTaskDescriptor(String id, String srcPortExpr, String tarPortExpr) {
		TaskDescriptor taskDesc = new TaskDescriptor(id, "", SetTask.class.getName());
		taskDesc.getInputVariables().add(Variables.newInstance("source", "", srcPortExpr));
		taskDesc.getOutputVariables().add(Variables.newInstance("target", "", tarPortExpr));
		
		return taskDesc;
	}
	
	public static void loadVariablesFromOperation(DefaultElementReference opRef,
													TaskDescriptor descriptor) throws IOException {
		SubmodelElement sme = opRef.read();
		if ( !(sme instanceof Operation) ) {
			throw new IllegalArgumentException("Target SubmodelElement is not an Operation: " + sme);
		}
		Operation op = (Operation)sme;
		
		DefaultSubmodelReference smRef = (DefaultSubmodelReference)opRef.getSubmodelReference();
		FStream.from(op.getInputVariables())
			    .zipWithIndex()
			    .forEach(idxed -> {
			    	String varName = idxed.value().getValue().getIdShort();
			    	MDTArgumentReference argRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.INPUT, varName);
			    	descriptor.getInputVariables().add(new ReferenceVariable(varName, "", argRef));
			    });
		FStream.from(op.getOutputVariables())
			    .zipWithIndex()
			    .forEach(idxed -> {
			    	String varName = idxed.value().getValue().getIdShort();
			    	MDTArgumentReference argRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, varName);
			    	descriptor.getOutputVariables().add(new ReferenceVariable(varName, "", argRef));
			    });
		FStream.from(op.getInoutputVariables())
			    .zipWithIndex()
			    .forEach(idxed -> {
			    	String varName = idxed.value().getValue().getIdShort();
			    	MDTArgumentReference inArgRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.INPUT, varName);
			    	descriptor.getInputVariables().add(new ReferenceVariable(varName, "", inArgRef));
			    	
			    	MDTArgumentReference outArgRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, varName);
			    	descriptor.getOutputVariables().add(new ReferenceVariable(varName, "", outArgRef));
			    });

		Submodel sm = smRef.get().getSubmodel();
		if ( SubmodelUtils.isAISubmodel(sm) || SubmodelUtils.isSimulationSubmodel(sm) ) {
			descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		}

	}
	
	public static void loadVariablesFromSubmodel(TaskDescriptor task, MDTSubmodelReference ref) {
		Submodel submodel = ref.get().getSubmodel();
		if ( SubmodelUtils.isAISubmodel(submodel) ) {
			loadVariables(task, ref, "AI");
		}
		else if ( SubmodelUtils.isSimulationSubmodel(submodel) ) {
			loadVariables(task, ref, "Simulation");
		}
		else {
			String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
			throw new IllegalArgumentException("Unexpected Submodel: semanticId=" + semanticId);
		}
	}
	
	public static void loadSimulationVariables(TaskDescriptor task, DefaultSubmodelReference smRef) {
		loadVariables(task, smRef, "Simulation");
	}
	
	public static void loadAIVariables(TaskDescriptor task, DefaultSubmodelReference smRef) {
		loadVariables(task, smRef, "AI");
	}
	
	private static void loadVariables(TaskDescriptor task, MDTSubmodelReference smRef, String opTitle) {
		Submodel submodel = smRef.get().getSubmodel();
		
		SubmodelElementList inputs = SubmodelUtils.traverse(submodel, opTitle + "Info.Inputs",
															SubmodelElementList.class);
		FStream.from(inputs.getValue())
				.cast(SubmodelElementCollection.class)
				.zipWithIndex()
				.map(idxed -> {
					String idShortPath = String.format("%sInfo.Inputs[%d].InputValue", opTitle, idxed.index());
					DefaultElementReference ref = DefaultElementReference.newInstance(smRef, idShortPath);
					return toReferenceVariable(idxed.value(), ref, "Input");
				})
				.forEach(task.getInputVariables()::addIfAbscent);

		SubmodelElementList outputs = SubmodelUtils.traverse(submodel, opTitle + "Info.Outputs",
															SubmodelElementList.class);
		FStream.from(outputs.getValue())
				.cast(SubmodelElementCollection.class)
				.zipWithIndex()
				.map(idxed -> {
					String idShortPath = String.format("%sInfo.Outputs[%d].OutputValue", opTitle, idxed.index());
					DefaultElementReference ref = DefaultElementReference.newInstance(smRef, idShortPath);
					return toReferenceVariable(idxed.value(), ref, "Output");
				})
				.forEach(task.getOutputVariables()::addIfAbscent);
	}
	
	private static Variable toReferenceVariable(SubmodelElementCollection var, DefaultElementReference elmRef,
												String prefix) {
		String portName = SubmodelUtils.getPropertyById(var, prefix + "ID").value().getValue();
		String portDesc = SubmodelUtils.findPropertyById(var, prefix + "Description")
										.map(idxed -> idxed.value().getValue())
										.getOrNull();
		return Variables.newInstance(portName, portDesc, elmRef);
	}
	
	public static void update(MDTInstanceManager manager, TaskDescriptor descriptor,
							ProgramOperationDescriptor opDesc) {
		descriptor.getOptions().add(new MultiLineOption("commandLine", opDesc.getCommandLine()));
		FOption.accept(opDesc.getWorkingDirectory(), workDir -> {
			descriptor.addOrReplaceOption("workingDirectory", workDir.getAbsolutePath());
		});
		
		FOption.accept(opDesc.getOperationSubmodel(), ref -> {
			ref.activate(manager);
			loadVariablesFromSubmodel(descriptor, ref);
		});
		FStream.from(opDesc.getInputVariables()).forEach(descriptor.getInputVariables()::addOrReplace);
		FStream.from(opDesc.getOutputVariables()).forEach(descriptor.getOutputVariables()::addOrReplace);
		FOption.accept(opDesc.getTimeout(), to -> descriptor.addOrReplaceOption("timeout", to.toString()));
	}
	
	static class BaseBuilder<T extends BaseBuilder<T>> {
		protected final TaskDescriptor m_descriptor = new TaskDescriptor();
		
		protected BaseBuilder(String taskType) {
			m_descriptor.setType(taskType);
		}
		
		public TaskDescriptor build() {
			Preconditions.checkArgument(m_descriptor.getId() != null, "Task ID is null");
			
			return m_descriptor;
		}

		@SuppressWarnings("unchecked")
		public T id(String id) {
			m_descriptor.setId(id);
			return (T)this;
		}
		
		@SuppressWarnings("unchecked")
		public T name(String name) {
			m_descriptor.setName(name);
			return (T)this;
		}

		@SuppressWarnings("unchecked")
		public T description(String desc) {
			m_descriptor.setDescription(desc);
			return (T)this;
		}

		@SuppressWarnings("unchecked")
		public T addLabel(String name, String value) {
			m_descriptor.getLabels().add(new NameValue(name, value));
			return (T)this;
		}

		@SuppressWarnings("unchecked")
		public T addDependency(String... deps) {
			m_descriptor.getDependencies().addAll(List.of(deps));
			return (T)this;
		}

		@SuppressWarnings("unchecked")
		public T addOption(Option<?> opt) {
			m_descriptor.getOptions().add(opt);
			return (T)this;
		}
		
		public T addInputVariable(Variable var) {
			m_descriptor.getInputVariables().addOrReplace(var);
			return (T) this;
		}
		
		public T addOutputVariable(Variable var) {
			m_descriptor.getOutputVariables().addOrReplace(var);
			return (T) this;
		}
	}
	
	public static SetTaskBuilder setTaskBuilder() {
		return new SetTaskBuilder();
	}
	public static class SetTaskBuilder extends BaseBuilder<SetTaskBuilder> {
		private SetTaskBuilder() {
			super(SetTask.class.getName());
		}
		
		public TaskDescriptor build() {
			Preconditions.checkArgument(m_descriptor.getInputVariables().containsKey("source"),
										"Source variable is missing");
			Preconditions.checkArgument(m_descriptor.getOutputVariables().containsKey("target"),
										"Target variable is missing");

			return super.build();
		}
		
		public SetTaskBuilder source(ElementReference ref) {
			m_descriptor.getInputVariables().addOrReplace(Variables.newInstance("source", "", ref));
			return this;
		}
		public SetTaskBuilder source(String refExpr) {
			return source(ElementReferences.parseExpr(refExpr));
		}
		public SetTaskBuilder value(String literalExpr) {
			ElementValue literal = ElementValues.parseExpr(literalExpr);
			m_descriptor.getInputVariables().addOrReplace(Variables.newInstance("source", "", literal));
			return this;
		}
		
		public SetTaskBuilder target(ElementReference ref) {
			m_descriptor.getOutputVariables().addOrReplace(Variables.newInstance("target", "", ref));
			return this;
		}
		public SetTaskBuilder target(String refExpr) {
			return target(ElementReferences.parseExpr(refExpr));
		}
	}

	public static HttpTaskBuilder httpTaskBuilder() {
		return new HttpTaskBuilder();
	}
	public static class HttpTaskBuilder extends BaseBuilder<HttpTaskBuilder> {
		private HttpTaskBuilder() {
			super(HttpTask.class.getName());
		}
		
		public TaskDescriptor build() {
			KeyedValueList<String,Option<?>> options = m_descriptor.getOptions();
			Preconditions.checkState(options.containsKey(HttpTask.OPTION_SERVER_ENDPOINT), "ServerEndpoint is missing");
			Preconditions.checkState(options.containsKey(HttpTask.OPTION_OPERATION), "Operation ID is missing");
			Preconditions.checkState(options.containsKey(HttpTask.OPTION_POLL_INTERVAL), "PollInterval is missing");
			
			return super.build();
		}
		
		public HttpTaskBuilder serverEndpoint(String url) {
			m_descriptor.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, url);
			return this;
		}
		
		public HttpTaskBuilder operationId(String id) {
			m_descriptor.addOrReplaceOption(HttpTask.OPTION_OPERATION, id);
			return this;
		}
		
		public HttpTaskBuilder pollInterval(String interval) {
			m_descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, interval);
			return this;
		}
		
		public HttpTaskBuilder timeout(String timeout) {
			m_descriptor.addOrReplaceOption(HttpTask.OPTION_TIMEOUT, timeout);
			return this;
		}
		
		public HttpTaskBuilder sync(boolean flag) {
			m_descriptor.addOrReplaceOption(HttpTask.OPTION_SYNC, ""+flag);
			return this;
		}
		
		public HttpTaskBuilder operationSubmodelRef(DefaultSubmodelReference ref) {
			Preconditions.checkArgument(ref.isActivated(), "Operation (AI or Simulation) SubmodelReference is not activated");
			TaskDescriptors.loadVariablesFromSubmodel(m_descriptor, ref);
			
			return this;
		}
	}

	public static AASOperationTaskBuilder aasOperationTaskBuilder() {
		return new AASOperationTaskBuilder();
	}
	public static class AASOperationTaskBuilder extends BaseBuilder<AASOperationTaskBuilder> {
		private AASOperationTaskBuilder() {
			super(AASOperationTask.class.getName());
		}
		
		public TaskDescriptor build() {
			KeyedValueList<String,Option<?>> options = m_descriptor.getOptions();
			Preconditions.checkState(options.containsKey(AASOperationTask.OPTION_OPERATION),
														"AASOperation ElementReference is missing");
			Preconditions.checkState(options.containsKey(AASOperationTask.OPTION_POLL_INTERVAL),
														"PollInterval is missing");

			return super.build();
		}
		
		public AASOperationTaskBuilder operationRef(MDTElementReference opRef) {
			m_descriptor.addOrReplaceOption(AASOperationTask.OPTION_OPERATION, opRef);
			return this;
		}

		public AASOperationTaskBuilder pollInterval(String interval) {
			m_descriptor.addOrReplaceOption(AASOperationTask.OPTION_POLL_INTERVAL, interval);
			return this;
		}
		
		public AASOperationTaskBuilder timeout(String to) {
			m_descriptor.addOrReplaceOption(AASOperationTask.OPTION_TIMEOUT, to);
			return this;
		}
		
		public AASOperationTaskBuilder sync(boolean flag) {
			m_descriptor.addOrReplaceOption(AASOperationTask.OPTION_SYNC, ""+flag);
			return this;
		}
		
		public AASOperationTaskBuilder updateOperationVariables(boolean flag) {
			m_descriptor.addOrReplaceOption(AASOperationTask.OPTION_UPDATE_OPVARS, ""+flag);
			return this;
		}
	}

	public static TaskDescriptor from(MDTSubmodelReference opSubmodelRef) throws ModelValidationException, IOException {
		Submodel submodel = opSubmodelRef.get().getSubmodel();
		
		List<LangStringNameType> lsntList = submodel.getDisplayName();
		String name = (lsntList != null && lsntList.size() > 0) ? lsntList.get(0).getText() : "";
		List<LangStringTextType> lsttList = submodel.getDescription();
		String desc = (lsttList != null && lsttList.size() > 0) ? lsttList.get(0).getText() : "";
        TaskDescriptor descriptor = new TaskDescriptor(submodel.getIdShort(), name, desc);
		
		List<Qualifier> qualifiers = submodel.getQualifiers();
		String method = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_METHOD)
								.getOrThrow(() -> new ModelValidationException(
										"Submodel operation method not found: submodel idShort=" + submodel.getIdShort()));
		switch ( method ) {
			case "http":
				descriptor = loadHttpTask(descriptor, submodel);
				break;
			default:
                throw new ModelValidationException("Unsupported operation method: " + method);
		}
		
		loadVariablesFromSubmodel(descriptor, opSubmodelRef);
		
		return descriptor;
	}

	private static final String DEFAULT_POLL_INTERVAL = "1s";
	private static TaskDescriptor loadHttpTask(TaskDescriptor descriptor, Submodel submodel)
		throws ModelValidationException {
        descriptor.setType(HttpTask.class.getName());
        
        List<Qualifier> qualifiers = submodel.getQualifiers();
        String serverEndpoint
                = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_SERVER_ENDPOINT)
                            .getOrThrow(() -> new ModelValidationException("Submodel operation server endpoint not found: submodel idShort="
                                                                            + submodel.getIdShort()));
        descriptor.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, serverEndpoint);
        
        String opId = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_ID)
                                .getOrThrow(() -> new ModelValidationException("Submodel operation id is missing: submodel idShort="
                                                                                + submodel.getIdShort()));
        descriptor.addOrReplaceOption(HttpTask.OPTION_OPERATION, opId);
        
        String pollInterval = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_POLL_INTERVAL)
                                        	.getOrElse(DEFAULT_POLL_INTERVAL);
        descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, pollInterval);
        
        String timeout = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_TIMEOUT)
                                	.getOrNull();
        if ( timeout != null ) {
            descriptor.addOrReplaceOption(HttpTask.OPTION_TIMEOUT, timeout);
        }
        
        return descriptor;
	}
}
