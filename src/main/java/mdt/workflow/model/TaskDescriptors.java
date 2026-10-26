package mdt.workflow.model;

import java.io.IOException;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.stream.FStream;

import mdt.model.ReferenceUtils;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.SubmodelUtils.OperationSubmodelDescriptor;
import mdt.model.sm.SubmodelUtils.SubmodelArgumentDescriptor;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.task.AASOperationTask;
import mdt.task.SetTask;
import mdt.task.OperationSubmodelTask;
import mdt.task.RESTfulAsyncRpcTask;
import mdt.task.RESTfulRpcTask;
import mdt.task.TaskUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskDescriptors {
	private TaskDescriptors() {
		throw new AssertionError("Should not be called: class=" + TaskDescriptors.class.getName());
	}

	/**
	 * Python script를 실행하는 task의 type 식별자 ({@link TaskDescriptor#getType()} 값).
	 * <p>
	 * 이 type은 Airflow DAG의 mdtpy {@code PythonScriptOperator}로만 실행되며, 대응하는 Java
	 * {@link mdt.task.MDTTask} 구현 클래스는 <b>없다</b>. 다른 task type과 형식을 맞추기 위해 클래스 이름
	 * 형태로 두었을 뿐이므로, 이 문자열로 클래스를 로드해서는 안 된다.
	 */
	public static final String PYTHON_SCRIPT_TASK_TYPE = "mdt.task.PythonScriptTask";

	private final static Map<String,String> CLASS_TO_SHORT_ID_MAP = Map.of(
		AASOperationTask.class.getName(), "AASOperation",
		OperationSubmodelTask.class.getName(), "OperationSubmodel",
		RESTfulAsyncRpcTask.class.getName(), "RESTful",
		RESTfulRpcTask.class.getName(), "AIModel",
		SetTask.class.getName(), "Copy",
		PYTHON_SCRIPT_TASK_TYPE, "PythonScript"
	);
	
	public static String toShortTaskTypeId(String taskClassName) {
		String shortId = CLASS_TO_SHORT_ID_MAP.get(taskClassName);;
		if ( shortId == null ) {
			throw new IllegalArgumentException("unregistered task class: " + taskClassName);
		}
		
		return shortId;
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
			    	descriptor.addInputArgument(ArgumentSpec.reference(varName, argRef.toStringExpr()));
			    });
		FStream.from(op.getOutputVariables())
			    .zipWithIndex()
			    .forEach(idxed -> {
			    	String varName = idxed.value().getValue().getIdShort();
			    	MDTArgumentReference argRef = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, varName);
			    	descriptor.addOutputArgument(ArgumentSpec.reference(varName, argRef.toStringExpr()));
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
			String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
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
			task.addInputArgument(ArgumentSpec.reference(ent.getKey(), ref.toStringExpr()));
		}
		for ( Map.Entry<String, SubmodelArgumentDescriptor> ent: opSmDesc.getOutputs().entrySet() ) {
			SubmodelArgumentDescriptor argDesc = ent.getValue();
			MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.OUTPUT, argDesc.getId());
			task.addInputArgument(ArgumentSpec.reference(ent.getKey(), ref.toStringExpr()));
		}
	}
}
