package mdt.model.sm.data;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link LOT} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultLOT extends SubmodelElementCollectionEntity implements LOT {
	@PropertyField(idShort="LotId") private String lotId;
	@PropertyField(idShort="ItemId") private String itemId;
	@PropertyField(idShort="State") private String state;
	@PropertyField(idShort="Quantity") private Integer quantity;
	@PropertyField(idShort="OperationId") private String operationId;
	@PropertyField(idShort="EquipmentId") private String equipmentId;
	@PropertyField(idShort="StartDateTime") private Timestamp startDateTime;
	@PropertyField(idShort="EndDateTime") private Timestamp endDateTime;
	@PropertyField(idShort="AppliedTactTime") private Float appliedTactTime;
	
	@Override
	public String getIdShort() {
		return this.lotId;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getLotId());
	}
}
