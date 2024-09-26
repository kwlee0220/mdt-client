package mdt.task.skku;

import lombok.Getter;
import lombok.Setter;
import mdt.model.PropertyField;
import mdt.model.UserDefinedSMC;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class ProcessTaktTime extends UserDefinedSMC {
	@PropertyField(idShort="MachineId") private String machineId;
	@PropertyField(idShort="MachineName") private String machineName;
	@PropertyField(idShort="TaktTimeDist") private String taktTimeDist;
	@PropertyField(idShort="TaktTimeLoc", valueType="Double") private Double taktTimeLoc;
	@PropertyField(idShort="TaktTimeScale", valueType="Double") private Double taktTimeScale;
	
	public ProcessTaktTime() {
		super(null, "machineId");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getMachineId());
	}
}
