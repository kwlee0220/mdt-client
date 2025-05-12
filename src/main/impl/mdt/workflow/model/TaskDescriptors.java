package mdt.workflow.model;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;

import lombok.experimental.UtilityClass;

import utils.KeyedValueList;
import utils.UnitUtils;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.NameValue;
import mdt.model.ReferenceUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.ProgramOperationDescriptor;
import mdt.task.builtin.SetTask;


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
	
	public static void loadVariables(TaskDescriptor task, MDTSubmodelReference ref) {
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
			descriptor.getOptions().add(new FileOption("workingDirectory", workDir));
		});
		
		FOption.accept(opDesc.getOperationSubmodel(), ref -> {
			ref.activate(manager);
			loadVariables(descriptor, ref);
		});
		FStream.from(opDesc.getInputVariables()).forEach(descriptor.getInputVariables()::addOrReplace);
		FStream.from(opDesc.getOutputVariables()).forEach(descriptor.getOutputVariables()::addOrReplace);
		FOption.accept(opDesc.getTimeout(), to -> descriptor.getOptions().add(new DurationOption("timeout", to)));
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
			Preconditions.checkArgument(m_descriptor.getInputVariables().containsKey("source"), "Source variable is missing");
			Preconditions.checkArgument(m_descriptor.getOutputVariables().containsKey("target"), "Target variable is missing");

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
			m_descriptor.getInputVariables().addOrReplace(Variables.newInstance("target", "", ref));
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
			m_descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, url);
			return this;
		}
		
		public HttpTaskBuilder operationId(String id) {
			m_descriptor.addOption(HttpTask.OPTION_OPERATION, id);
			return this;
		}
		
		public HttpTaskBuilder pollInterval(Duration interval) {
			m_descriptor.addOption(HttpTask.OPTION_POLL_INTERVAL, interval);
			return this;
		}
		public HttpTaskBuilder pollInterval(String intvlStr) {
			return pollInterval(UnitUtils.parseDuration(intvlStr));
		}
		
		public HttpTaskBuilder timeout(Duration to) {
			m_descriptor.addOption(HttpTask.OPTION_TIMEOUT, to);
			return this;
		}
		public HttpTaskBuilder timeout(String toStr) {
			return timeout(UnitUtils.parseDuration(toStr));
		}
		
		public HttpTaskBuilder sync(boolean flag) {
			m_descriptor.addOption(HttpTask.OPTION_SYNC, flag);
			return this;
		}
		
		public HttpTaskBuilder operationSubmodelRef(DefaultSubmodelReference ref) {
			Preconditions.checkArgument(ref.isActivated(), "Operation (AI or Simulation) SubmodelReference is not activated");
			TaskDescriptors.loadVariables(m_descriptor, ref);
			
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
			Preconditions.checkState(options.containsKey(AASOperationTask.OPTION_OPERATION), "AASOperation ElementReference is missing");
			Preconditions.checkState(options.containsKey(AASOperationTask.OPTION_POLL_INTERVAL), "PollInterval is missing");

			return super.build();
		}
		
		public AASOperationTaskBuilder operationRef(MDTElementReference opRef) {
			m_descriptor.addOption(AASOperationTask.OPTION_OPERATION, opRef);
			return this;
		}
		
		public AASOperationTaskBuilder pollInterval(Duration interval) {
			m_descriptor.addOption(AASOperationTask.OPTION_POLL_INTERVAL, interval);
			return this;
		}
		public AASOperationTaskBuilder pollInterval(String intvlStr) {
			return pollInterval(UnitUtils.parseDuration(intvlStr));
		}
		
		public AASOperationTaskBuilder timeout(Duration to) {
			m_descriptor.addOption(AASOperationTask.OPTION_TIMEOUT, to);
			return this;
		}
		public AASOperationTaskBuilder timeout(String toStr) {
			return timeout(UnitUtils.parseDuration(toStr));
		}
		
		public AASOperationTaskBuilder sync(boolean flag) {
			m_descriptor.addOption(AASOperationTask.OPTION_SYNC, flag);
			return this;
		}
		
		public AASOperationTaskBuilder updateOperationVariables(boolean flag) {
			m_descriptor.addOption(AASOperationTask.OPTION_UPDATE_OPVARS, flag);
			return this;
		}
	}
}
