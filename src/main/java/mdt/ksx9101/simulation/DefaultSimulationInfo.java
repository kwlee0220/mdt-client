package mdt.ksx9101.simulation;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.SMCField;
import mdt.model.SMLField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationInfo extends AbstractMDTSubmodelElementCollection
										implements SimulationInfo {
	@SMCField(idShort="SimulationTool", adaptorClass=DefaultSimulationTool.class)
	private SimulationTool simulationTool;
	
	@SMLField(idShort="Inputs", elementClass=DefaultSimulationInput.class)
	private List<SimulationInput> inputs = Lists.newArrayList();
	
	@SMLField(idShort="Outputs", elementClass=DefaultSimulationOutput.class)
	private List<SimulationOutput> outputs = Lists.newArrayList();
	
	public DefaultSimulationInfo() {
		super("SimulationInfo", null);
	}
}
