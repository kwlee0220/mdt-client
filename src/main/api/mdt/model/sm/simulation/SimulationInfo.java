package mdt.model.sm.simulation;

import mdt.model.MinResourceRequirements;
import mdt.model.sm.OperationInfo;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationInfo extends OperationInfo {
	public Model getModel();
	public void setModel(Model model);
	
	public MinResourceRequirements getMinResourceRequirements();
	public void setMinResourceRequirements(MinResourceRequirements format);

	public SimulationTool getSimulationTool();
	public void setSimulationTool(SimulationTool id);
}
