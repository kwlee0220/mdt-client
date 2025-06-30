package mdt.task.builtin;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;

import lombok.experimental.UtilityClass;

import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.PropertyValue;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class TaskUtils {
	public static final String LABEL_MDT_OPERATION = "mdt-operation";
	static DefaultElementReference loadLastExecutionTimeRef(MDTInstanceManager manager,
															TaskDescriptor descriptor) throws TaskException {
		DefaultSubmodelReference opSmRef
						= descriptor.findLabel(LABEL_MDT_OPERATION)
									.map(smExprStr -> MDTExprParser.parseSubmodelReference(smExprStr).evaluate())
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

	static void updateLastExecutionTime(MDTInstanceManager manager, TaskDescriptor m_descriptor,
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
}
