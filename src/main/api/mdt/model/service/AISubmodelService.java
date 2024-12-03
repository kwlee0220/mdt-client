package mdt.model.service;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;

import mdt.model.sm.ai.AI;
import mdt.model.sm.ai.DefaultAI;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AISubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_service;
	private final DefaultAI m_ai;
	
	public AISubmodelService(SubmodelService service) {
		Submodel submodel = service.getSubmodel();
		Preconditions.checkArgument(submodel.getSemanticId().equals(AI.SEMANTIC_ID_REFERENCE),
									"Not AI Submodel, but=" + submodel); 
		
		m_service = service;
		
		m_ai = new DefaultAI();
		m_ai.updateFromAasModel(submodel);
	}
	
	public AI getAI() {
		return m_ai;
	}
}
