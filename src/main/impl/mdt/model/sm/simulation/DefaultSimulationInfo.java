package mdt.model.sm.simulation;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultMinResourceRequirements;
import mdt.model.MinResourceRequirements;
import mdt.model.sm.DefaultOperationInfo;
import mdt.model.sm.entity.SMCollectionField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationInfo extends DefaultOperationInfo implements SimulationInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMCollectionField(idShort="SimulationTool", adaptorClass=DefaultSimulationTool.class)
	private SimulationTool simulationTool;
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	public DefaultSimulationInfo() {
		super("SimulationInfo");
	}
}
