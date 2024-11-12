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
public class DefaultProductionPerformance extends SubmodelElementCollectionEntity
											implements ProductionPerformance {
	@PropertyField(idShort="ProductionPerformanceID") private String productionPerformanceID;
	@PropertyField(idShort="ProductionPerformanceSequence") private String productionPerformanceSequence;
	@PropertyField(idShort="ProductionOrderID") private String productionOrderID;
	@PropertyField(idShort="ProductionOrderSequence") private String productionOrderSequence;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="ItemUOMCode") private String itemUOMCode;
	@PropertyField(idShort="ProducedQuantity") private String producedQuantity;
	@PropertyField(idShort="DefectQuantity") private String defectQuantity;
	@PropertyField(idShort="OperationID") private String operationID;
	@PropertyField(idShort="OperationSequence") private String operationSequence;
	@PropertyField(idShort="ExecutionStartDateTime") private String executionStartDateTime;
	@PropertyField(idShort="ExecutionEndDateTime") private String executionEndDateTime;
	@PropertyField(idShort="CancelIndicator") private String cancelIndicator;
	@PropertyField(idShort="CancelDateTime") private String cancelDateTime;
	@PropertyField(idShort="LotID") private String lotID;
	@PropertyField(idShort="BatchID") private String batchID;
	
	@Override
	public String getIdShort() {
		return "" + this.productionOrderID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getProductionPerformanceID());
	}
}
