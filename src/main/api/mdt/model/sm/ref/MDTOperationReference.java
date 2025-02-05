package mdt.model.sm.ref;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;

import utils.InternalException;

import mdt.model.ReferenceUtils;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.simulation.Simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
final class MDTOperationReference implements MDTElementReference {
	private final MDTSubmodelReference m_smRef;
	
	@Delegate private volatile DefaultElementReference m_opInfoRef;
	private volatile String m_opInfoPath;
	
	private MDTOperationReference(MDTSubmodelReference smRef) {
		Preconditions.checkArgument(smRef != null);
		
		m_smRef = smRef;
	}
	
	public MDTSubmodelReference getSubmodelReference() {
		return m_smRef;
	}

	@Override
	public boolean isActivated() {
		return m_smRef.isActivated();
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_smRef.activate(manager);
		
		SubmodelService opSubmodelSvc = m_smRef.get();
		Submodel opSubmodel = opSubmodelSvc.getSubmodel();
		
		String semanticIdStr = ReferenceUtils.getSemanticIdStringOrNull(opSubmodel.getSemanticId());
		m_opInfoPath = switch ( semanticIdStr ) {
			case AI.SEMANTIC_ID -> "AIInfo";
			case Simulation.SEMANTIC_ID -> "SimulationInfo";
			default -> throw new InternalException("Unexpected Operation type: " + semanticIdStr);
		};
		m_opInfoRef = DefaultElementReference.newInstance(m_smRef, m_opInfoPath);
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_smRef.getInstanceId(), m_smRef.getSubmodelIdShort());
	}
	
	static MDTOperationReference newInstance(MDTSubmodelReference smRef) {
		return new MDTOperationReference(smRef);
	}
}
