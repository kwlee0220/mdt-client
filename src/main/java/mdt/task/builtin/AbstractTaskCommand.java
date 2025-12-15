package mdt.task.builtin;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.ModelValidationException;
import mdt.model.Qualifiers;
import mdt.model.ReferenceUtils;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractTaskCommand extends MultiVariablesCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractTaskCommand.class);

	@Option(names={"--submodel"}, paramLabel="submodel reference",
			description="Target AI/Simulation submodel reference (e.g. <instance-id>/<submodel-idshort>)")
	private String m_opSmRefExpr;
	
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	private String m_timeout = null;
	
	public AbstractTaskCommand() {
		setLogger(s_logger);
	}
	
	protected void loadTaskDescriptor(TaskDescriptor descriptor, MDTInstanceManager manager) {
        // Submodel reference가 지정된 경우, 해당 SubmodelReference에 대한 Task descriptor를 생성한다.
		if ( m_opSmRefExpr != null ) {
			loadFromMDTOperationSubmodel(manager, descriptor, m_opSmRefExpr);
        }

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromArguments(manager, descriptor);
		
		FOption.accept(m_timeout, to -> descriptor.addOption(HttpTask.OPTION_TIMEOUT, to));
	}

	private void loadFromMDTOperationSubmodel(MDTInstanceManager manager, TaskDescriptor descriptor,
												String opSmRefExpr) throws ModelValidationException {
		DefaultSubmodelReference opSmRef = MDTExpressionParser.parseSubmodelReference(opSmRefExpr).evaluate();
		opSmRef.activate(manager);
		Submodel opSubmodel = opSmRef.get().getSubmodel();
		descriptor.setId(opSubmodel.getIdShort());
		
		// Submodel의 displayName과 description을 이용하여 TaskDescriptor의 id와 name을 설정한다.
		List<LangStringNameType> lsntList = opSubmodel.getDisplayName();
		String name = (lsntList != null && lsntList.size() > 0) ? lsntList.get(0).getText() : "";
		descriptor.setName(name);
		
		List<LangStringTextType> lsttList = opSubmodel.getDescription();
		String desc = (lsttList != null && lsttList.size() > 0) ? lsttList.get(0).getText() : "";
		descriptor.setDescription(desc);
		
        // Submodel의 qualifiers를 이용하여 TaskDescriptor의 옵션들을 설정한다.
		List<Qualifier> qualifiers = opSubmodel.getQualifiers();
		String method = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_METHOD)
									.getOrThrow(() -> new ModelValidationException(
															"Submodel operation method is not found: submodel idShort="
															+ opSubmodel.getIdShort()));
		switch ( method ) {
			case "http":
				descriptor = loadHttpTask(descriptor, opSubmodel);
				break;
			default:
                throw new ModelValidationException("Unsupported operation method: " + method);
		}
		
		// AI/Simulation Submodel의 경우, input/output parameter들을 TaskDescriptor에 반영한다.
		loadVariablesFromSubmodel(descriptor, opSmRef);
        descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, opSmRefExpr);
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
        descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, serverEndpoint);
        
        String opId = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_ID)
                                .getOrThrow(() -> new ModelValidationException("Submodel operation id is missing: submodel idShort="
                                                                                + submodel.getIdShort()));
        descriptor.addOption(HttpTask.OPTION_OPERATION, opId);
        
        String pollInterval = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_POLL_INTERVAL)
                                        	.getOrElse(DEFAULT_POLL_INTERVAL);
        descriptor.addOption(HttpTask.OPTION_POLL_INTERVAL, pollInterval);
        
        String timeout = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_TIMEOUT)
                                	.getOrNull();
        if ( timeout != null ) {
            descriptor.addOption(HttpTask.OPTION_TIMEOUT, timeout);
        }
        
        return descriptor;
	}
	
	private void loadVariablesFromSubmodel(TaskDescriptor task, MDTSubmodelReference ref) {
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
	
	private void loadVariables(TaskDescriptor task, MDTSubmodelReference smRef, String opTitle) {
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
	
	private Variable toReferenceVariable(SubmodelElementCollection var, DefaultElementReference elmRef,
										String prefix) {
		String portName = SubmodelUtils.getPropertyById(var, prefix + "ID").value().getValue();
		String portDesc = SubmodelUtils.findPropertyById(var, prefix + "Description")
										.map(idxed -> idxed.value().getValue())
										.orElse(null);
		return Variables.newInstance(portName, portDesc, elmRef);
	}
}
