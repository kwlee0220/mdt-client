package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.SubmodelService;
import mdt.model.sm.OperationSubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimulationSubmodelService extends OperationSubmodelService {
	public SimulationSubmodelService(SubmodelService service) {
		super(service);
	}
	
	public Simulation getSimulation() {
		Submodel submodel = m_service.getSubmodel();
		DefaultSimulation sim = new DefaultSimulation();
		sim.updateFromAasModel(submodel);
		
		return sim;
	}
}
