package mdt.task;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;

import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.PropertyValue;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class TaskUtils {
	private TaskUtils() {
		throw new AssertionError("Should not be called: class=" + TaskUtils.class.getName());
	}
	
	public static final String LABEL_MDT_OPERATION = "mdt-operation";
	static DefaultElementReference loadLastExecutionTimeRef(MDTInstanceManager manager,
															TaskDescriptor descriptor) throws TaskException {
		DefaultSubmodelReference opSmRef
						= descriptor.findLabel(LABEL_MDT_OPERATION)
									.map(smExprStr -> MDTExpressionParser.parseSubmodelReference(smExprStr).evaluate())
									.getOrNull();
		if ( opSmRef != null ) {
			opSmRef.activate(manager);
			Submodel submodel = opSmRef.get().getSubmodel();
			String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
			if ( AI.SEMANTIC_ID.equals(semanticId) ) {
				return DefaultElementReference.newInstance(opSmRef, "AIInfo.Model.LastExecutionTime");
			}
			else if ( Simulation.SEMANTIC_ID.equals(semanticId) ) {
				return DefaultElementReference.newInstance(opSmRef, "SimulationInfo.Model.LastExecutionTime");
			}
			else {
				throw new TaskException("Invalid Submodel for 'LastExecutionTime': smRef=" + opSmRef);
			}
		}
		
		return null;
	}

	public static void updateLastExecutionTime(MDTInstanceManager manager, TaskDescriptor m_descriptor,
										Instant startTime, Logger logger)
		throws TaskException {
		// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
		MDTElementReference lastExecTimeRef = loadLastExecutionTimeRef(manager, m_descriptor);
		if ( lastExecTimeRef != null ) {
			Duration execTime = Duration.between(startTime, Instant.now());
			try {
				lastExecTimeRef.updateValue(PropertyValue.DURATION(execTime));
			}
			catch ( ResourceNotFoundException | IOException expected ) {
				logger.warn("Failed to update 'LastExecutionTime', cause=" + expected);
			}
		}
	}

	/**
	 * 태스크 입력 인자 명세의 현재 값을 읽는다.
	 * <ul>
	 *   <li>참조 인자: 표현식을 해석·활성화해 가리키는 요소의 값을 읽는다.</li>
	 *   <li>리터럴 인자: 명세의 값을 돌려준다.</li>
	 *   <li>task-output 인자: 선행 태스크의 결과로부터 실행 엔진이 정해야 하므로 읽을 수 없다.</li>
	 * </ul>
	 * 2026-11-01 이전에는 {@code ArgumentSpec.readValue(MDTInstanceManager)}였고, task-output 인자이면
	 * {@code IllegalStateException}을 던졌다.
	 *
	 * @param arg		입력 인자 명세.
	 * @param manager	참조를 활성화할 MDT 인스턴스 관리자.
	 * @return	인자 값.
	 * @throws IOException		참조가 가리키는 값을 읽는 중 오류가 발생한 경우.
	 * @throws TaskException	task-output 인자인 경우.
	 */
	public static ElementValue readArgumentValue(ArgumentSpec arg, MDTInstanceManager manager)
		throws IOException, TaskException {
		return switch ( arg ) {
			case ArgumentSpec.ReferenceArgument refArg -> refArg.resolveReference(manager).readValue();
			case ArgumentSpec.LiteralArgument litArg -> litArg.getValue();
			case ArgumentSpec.TaskOutputArgument outArg
				-> throw new TaskException(String.format("task-output argument is not resolved: id=%s, source=%s:%s",
														outArg.getId(), outArg.getTaskId(), outArg.getOutputArgumentId()));
		};
	}
}
