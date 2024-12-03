package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionOrder extends SubmodelElementCollectionEntity implements ProductionOrder {
	@PropertyField(idShort="ProductionOrderID") private String productionOrderID;
	@PropertyField(idShort="ProductionOrderSequence") private String productionOrderSequence;
	@PropertyField(idShort="OperationID") private String operationID;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="ItemUOMCode") private String itemUOMCode;
	@PropertyField(idShort="ProductionOrderQuantity") private String productionOrderQuantity;
	@PropertyField(idShort="ProductionDueDateTime") private String productionDueDateTime;
	@PropertyField(idShort="ScheduleStartDateTime") private String scheduleStartDateTime;
	@PropertyField(idShort="ScheduleEndDateTime") private String scheduleEndDateTime;
	@PropertyField(idShort="SalesDocumentNumber") private String salesDocumentNumber;
	@PropertyField(idShort="SalesDocumentSequence") private String salesDocumentSequence;
	
	@Override
	public String getIdShort() {
		return "" + this.productionOrderID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.productionOrderID);
	}
}