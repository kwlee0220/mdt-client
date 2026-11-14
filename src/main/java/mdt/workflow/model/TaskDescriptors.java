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


/**
 * {@link TaskDescriptor} 생성·구성을 돕는 유틸리티 메소드 모음.
 * <p>
 * task 클래스 이름과 짧은 type 식별자 사이의 변환, AAS 연산 또는 AI/Simulation Submodel로부터
 * 입출력 인자 명세를 채우는 기능을 제공한다.
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
	
	/**
	 * task 클래스 이름(또는 {@link #PYTHON_SCRIPT_TASK_TYPE})에 해당하는 짧은 type 식별자를 반환한다.
	 * <p>
	 * 예: {@code mdt.task.AASOperationTask} → {@code "AASOperation"},
	 * {@code mdt.task.SetTask} → {@code "Copy"}.
	 *
	 * @param taskClassName	task 클래스의 전체 이름.
	 * @return	짧은 type 식별자.
	 * @throws IllegalArgumentException	등록되지 않은 클래스 이름인 경우.
	 */
	public static String toShortTaskTypeId(String taskClassName) {
		String shortId = CLASS_TO_SHORT_ID_MAP.get(taskClassName);;
		if ( shortId == null ) {
			throw new IllegalArgumentException("unregistered task class: " + taskClassName);
		}
		
		return shortId;
	}
	
	/**
	 * 참조가 가리키는 AAS 연산의 입력·출력 변수마다 연산 인자 참조({@code oparg:...})를 가리키는 인자 명세를
	 * 만들어 기술자에 추가한다. 인자 이름은 변수의 idShort이다.
	 * <p>
	 * 연산이 속한 Submodel이 AI 또는 Simulation Submodel이면 {@link TaskDescriptor#LABEL_MDT_OPERATION} 레이블에
	 * 그 Submodel 참조 표현식을 추가한다. 입출력(inoutput) 변수는 다루지 않는다.
	 *
	 * @param opRef			활성화된 연산 element 참조. Submodel 참조가 {@link DefaultSubmodelReference}여야 한다.
	 * @param descriptor	인자 명세를 추가할 기술자.
	 * @throws IOException	연산 element나 Submodel을 읽는 중 오류가 발생한 경우.
	 * @throws IllegalArgumentException	참조가 가리키는 요소가 {@link Operation}이 아닌 경우.
	 */
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
			descriptor.addLabel(TaskDescriptor.LABEL_MDT_OPERATION, smRef.toStringExpr());
		}
	}
	
	/**
	 * AI 또는 Simulation Submodel의 인자 기술자를 읽어, 각 입력·출력 인자의 연산 인자 참조를 가리키는
	 * 인자 명세를 기술자에 추가한다.
	 *
	 * @param task	인자 명세를 추가할 기술자.
	 * @param ref	활성화된 AI 또는 Simulation Submodel 참조.
	 * @throws IllegalArgumentException	Submodel이 AI나 Simulation Submodel이 아닌 경우.
	 */
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
	
	/**
	 * Simulation Submodel의 인자 기술자로부터 입력·출력 인자 명세를 기술자에 추가한다.
	 * Submodel의 종류는 확인하지 않는다.
	 *
	 * @param task	인자 명세를 추가할 기술자.
	 * @param smRef	활성화된 Simulation Submodel 참조.
	 */
	public static void loadSimulationVariables(TaskDescriptor task, DefaultSubmodelReference smRef) {
		loadArgumentSpecs(task, smRef, "Simulation");
	}
	
	/**
	 * AI Submodel의 인자 기술자로부터 입력·출력 인자 명세를 기술자에 추가한다.
	 * Submodel의 종류는 확인하지 않는다.
	 *
	 * @param task	인자 명세를 추가할 기술자.
	 * @param smRef	활성화된 AI Submodel 참조.
	 */
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
			// 2026-11-01 이전에는 addInputArgument를 불러 출력 인자가 입력 인자로 등록됐다.
			task.addOutputArgument(ArgumentSpec.reference(ent.getKey(), ref.toStringExpr()));
		}
	}
}
