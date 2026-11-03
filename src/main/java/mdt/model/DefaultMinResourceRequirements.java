package mdt.model;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link MinResourceRequirements}의 기본 구현으로, 최소 자원 요구사항을
 * SubmodelElementCollection 엔티티로 표현한다.
 * <p>
 * 각 필드는 같은 이름의 하위 Property(GPUSpec, GPUNum, CPUSpec, MemorySize, MemorySpeed)에 매핑된다.
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
}
