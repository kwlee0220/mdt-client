package mdt.workflow.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;

import utils.stream.FStream;

import mdt.model.ModelValidationException;
import mdt.model.Qualifiers;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.SubmodelUtils.OperationSubmodelDescriptor;
import mdt.model.sm.SubmodelUtils.SubmodelArgumentDescriptor;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.SetTask;
import mdt.task.builtin.TaskUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class TaskDescriptors {
	private final static Map<String,String> CLASS_TO_SHORT_ID_MAP = Map.of(
		AASOperationTask.class.getName(), "AASOperation",
		HttpTask.class.getName(), "Http",
		SetTask.class.getName(), "Set"
	);
	
	public static String toShortTaskTypeId(String taskClassName) {
		String shortId = CLASS_TO_SHORT_ID_MAP.get(taskClassName);;
		if ( shortId == null ) {
			throw new IllegalArgumentException("unregistered task class: " + taskClassName);
		}
		
		return shortId;
	}
		
	public static TaskDescriptor newSetTaskDescriptor(String id, String srcArgSpec, String tarArgSpec) {
		TaskDescriptor taskDesc = new TaskDescriptor(id, "", SetTask.class.getName());
		taskDesc.addInputArgumentSpec("source", ArgumentSpec.parseArgumentSpec(srcArgSpec));
		taskDesc.addOutputArgumentSpec("target", ArgumentSpec.reference(tarArgSpec));
		
		return taskDesc;
	}
	
	public static void loadVariablesFromAASOperation(DefaultElementReference opRef,
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
			    	descriptor.addInputArgumentSpec(varName, ArgumentSpec.reference(argRef));
			    });
		FStream.from(op.getOutputVariables())
			    .zipWithIndex()
			    .forEach(idxed -> {
			    	String varName = idxed.value().getValue().getIdShort();
			    	MDTArgumentReference argRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, varName);
			    	descriptor.addOutputArgumentSpec(varName, ArgumentSpec.reference(argRef));
			    });

		Submodel sm = smRef.get().getSubmodel();
		if ( SubmodelUtils.isAISubmodel(sm) || SubmodelUtils.isSimulationSubmodel(sm) ) {
			descriptor.addLabel(TaskUtils.LABEL_MDT_OPERATION, smRef.toStringExpr());
		}
	}
	
	public static void loadArgumentSpecsFromSubmodel(TaskDescriptor task, DefaultSubmodelReference ref) {
		Submodel submodel = ref.get().getSubmodel();
		if ( SubmodelUtils.isAISubmodel(submodel) ) {
			loadArgumentSpecs(task, ref, "AI");
		}
		else if ( SubmodelUtils.isSimulationSubmodel(submodel) ) {
			loadArgumentSpecs(task, ref, "Simulation");
		}
		else {
			String semanticId = SubmodelUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
			throw new IllegalArgumentException("Unexpected Submodel: semanticId=" + semanticId);
		}
	}
	
	public static void loadSimulationVariables(TaskDescriptor task, DefaultSubmodelReference smRef) {
		loadArgumentSpecs(task, smRef, "Simulation");
	}
	
	public static void loadAIVariables(TaskDescriptor task, DefaultSubmodelReference smRef) {
		loadArgumentSpecs(task, smRef, "AI");
	}
	
	private static void loadArgumentSpecs(TaskDescriptor task, DefaultSubmodelReference smRef, String opTitle) {
		Submodel submodel = smRef.get().getSubmodel();
		
		OperationSubmodelDescriptor opSmDesc = SubmodelUtils.loadOperationSubmodelDescriptor(submodel);
		
		for ( Map.Entry<String, SubmodelArgumentDescriptor> ent: opSmDesc.getInputs().entrySet() ) {
			SubmodelArgumentDescriptor argDesc = ent.getValue();
			MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.INPUT, argDesc.getId());
			task.addInputArgumentSpec(ent.getKey(), ArgumentSpec.reference(ref));
		}
		for ( Map.Entry<String, SubmodelArgumentDescriptor> ent: opSmDesc.getOutputs().entrySet() ) {
			SubmodelArgumentDescriptor argDesc = ent.getValue();
			MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, argDesc.getId());
			task.addInputArgumentSpec(ent.getKey(), ArgumentSpec.reference(ref));
		}
	}

	public static TaskDescriptor from(DefaultSubmodelReference opSubmodelRef)
		throws ModelValidationException, IOException {
		Submodel submodel = opSubmodelRef.get().getSubmodel();
		
		List<LangStringNameType> lsntList = submodel.getDisplayName();
		String name = (lsntList != null && lsntList.size() > 0) ? lsntList.get(0).getText() : "";
		List<LangStringTextType> lsttList = submodel.getDescription();
		String desc = (lsttList != null && lsttList.size() > 0) ? lsttList.get(0).getText() : "";
        TaskDescriptor descriptor = new TaskDescriptor(submodel.getIdShort(), name, desc);
		
		List<Qualifier> qualifiers = submodel.getQualifiers();
		String method = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_METHOD)
								.orElseThrow(() -> new ModelValidationException(
										"Submodel operation method not found in the submodel qualifiers: submodel idShort="
										+ submodel.getIdShort()));
		switch ( method ) {
			case "http":
				descriptor = loadHttpTask(descriptor, submodel);
				break;
			default:
                throw new ModelValidationException("Unsupported operation method: " + method);
		}
		
		loadArgumentSpecsFromSubmodel(descriptor, opSubmodelRef);
		
		return descriptor;
	}

	private static final String DEFAULT_POLL_INTERVAL = "1s";
	private static TaskDescriptor loadHttpTask(TaskDescriptor descriptor, Submodel submodel)
		throws ModelValidationException {
        descriptor.setType(HttpTask.class.getName());
        
        List<Qualifier> qualifiers = submodel.getQualifiers();
        String serverEndpoint
                = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_SERVER_ENDPOINT)
                            .orElseThrow(() -> new ModelValidationException("Submodel operation server endpoint not found: submodel idShort="
                                                                            + submodel.getIdShort()));
        descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, serverEndpoint);
        
        String opId = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_ID)
                                .orElseThrow(() -> new ModelValidationException("Submodel operation id is missing: submodel idShort="
                                                                                + submodel.getIdShort()));
        descriptor.addOption(HttpTask.OPTION_OPERATION, opId);
        
        String pollInterval = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_POLL_INTERVAL)
                                        	.orElse(DEFAULT_POLL_INTERVAL);
        descriptor.addOption(HttpTask.OPTION_POLL_INTERVAL, pollInterval);
        
        String timeout = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_TIMEOUT)
                                	.orElse(null);
        if ( timeout != null ) {
            descriptor.addOption(HttpTask.OPTION_TIMEOUT, timeout);
        }
        
        return descriptor;
	}
}
