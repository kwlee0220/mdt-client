package mdt.model;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultMinResourceRequirements extends SubmodelElementCollectionEntity
											implements MinResourceRequirements {
	@PropertyField(idShort="GPUSpec")
	private String GPUSpec;
	
	@PropertyField(idShort="GPUNum")
	private String GPUNum;
	
	@PropertyField(idShort="CPUSpec")
	private String CPUSpec;
	
	@PropertyField(idShort="MemorySize")
	private String MemorySize;
	
	@PropertyField(idShort="MemorySpeed")
	private String MemorySpeed;
	
	@PropertyField(idShort="InputType")
	private String inputType;
}
