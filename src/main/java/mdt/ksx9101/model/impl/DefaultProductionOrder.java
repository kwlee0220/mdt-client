package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ProductionOrder;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionOrder extends AbstractMDTSubmodelElementCollection implements ProductionOrder {
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
	
	public DefaultProductionOrder() {
		super(null, "productionOrderID");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.productionOrderID);
	}
}