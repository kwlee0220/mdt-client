package mdt.model.sm.simulation;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationTool extends SubmodelElementCollectionEntity implements SimulationTool {
	@PropertyField(idShort="SimToolName")
	private String simToolName;
	
	@PropertyField(idShort="OperatingSystem")
	private String operatingSystem;
	
	@PropertyField(idShort="LicenseModel")
	private String licenseModel;
	
	@PropertyField(idShort="SimulatorEndpoint")
	private String simulatorEndpoint;
	
	@PropertyField(idShort="SimulationTimeout", valueType="Duration")
	private Duration simulationTimeout;
	
	@PropertyField(idShort="SessionRetainTimeout", valueType="Duration")
	private Duration sessionRetainTimeout;
	
	public DefaultSimulationTool() {
		setIdShort("SimulationTool");
	}
}