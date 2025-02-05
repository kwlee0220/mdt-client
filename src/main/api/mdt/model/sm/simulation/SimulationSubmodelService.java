package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;

import mdt.model.SubmodelService;
import mdt.model.sm.simulation.DefaultSimulation;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimulationSubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_service;
	private final DefaultSimulation m_simulation;
	
	public SimulationSubmodelService(SubmodelService service) {
		Submodel submodel = service.getSubmodel();
		Preconditions.checkArgument(submodel.getSemanticId().equals(Simulation.SEMANTIC_ID_REFERENCE),
									"Not Simulation Submodel, but=" + submodel); 
		
		m_service = service;
		
		m_simulation = new DefaultSimulation();
		m_simulation.updateFromAasModel(submodel);
	}
	
	public Simulation getSimulation() {
		return m_simulation;
	}
}
