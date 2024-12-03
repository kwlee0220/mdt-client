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
public class DefaultProductionPlanning extends SubmodelElementCollectionEntity
											implements ProductionPlanning {
	@PropertyField(idShort="ProductionPlanID") private String productionPlanID;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="ProductionPlanQuantity") private String productionPlanQuantity;
	@PropertyField(idShort="ScheduleStartDateTime") private String scheduleStartDateTime;
	@PropertyField(idShort="ScheduleEndDateTime") private String scheduleEndDateTime;
	
	@Override
	public String getIdShort() {
		return this.productionPlanID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getProductionPlanID());
	}
}
