package mdt.ksx9101.simulation;

import java.util.List;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationInfo {
	public SimulationTool getSimulationTool();
	public void setSimulationTool(SimulationTool tool);
	
	public List<SimulationInput> getInputs();
	public void setInputs(List<SimulationInput> inputs);
	
	public List<SimulationOutput> getOutputs();
	public void setOutputs(List<SimulationOutput> outputs);
}
