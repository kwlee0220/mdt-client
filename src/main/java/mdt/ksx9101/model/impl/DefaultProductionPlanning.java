package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ProductionPlanning;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


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
	
	public DefaultProductionPlanning() {
		super(null, "productionPlanID");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getProductionPlanID());
	}
}
