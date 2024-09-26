package mdt.ksx9101.simulation;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationTool extends SubmodelElementCollectionEntity implements SimulationTool {
	@PropertyField(idShort="SimToolName")
	private String simToolName;
	
	@PropertyField(idShort="SimulatorEndpoint")
	private String simulatorEndpoint;
	
	@PropertyField(idShort="SimulationTimeout", valueType="Duration")
	private Duration simulationTimeout;
	
	@PropertyField(idShort="SessionRetainTimeout", valueType="Duration")
	private Duration sessionRetainTimeout;
	
	public DefaultSimulationTool() {
		super("SimulationTool", null);
	}
}
