package mdt.model.sm.ai;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.SubmodelService;
import mdt.model.sm.OperationSubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AISubmodelService extends OperationSubmodelService {
	public AISubmodelService(SubmodelService service) {
		super(service);
	}
	
	public AI getAI() {
		Submodel submodel = m_service.getSubmodel();
		DefaultAI ai = new DefaultAI();
		ai.updateFromAasModel(submodel);
		
		return ai;
	}
}
