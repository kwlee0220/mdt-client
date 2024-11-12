package mdt.model.sm.data;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
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
