package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ProductionPlanning;
import mdt.ksx9101.model.ProductionPlannings;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionPlannings
		extends SubmodelElementListEntity<ProductionPlanning,DefaultProductionPlanning>
											implements ProductionPlannings {
	public DefaultProductionPlannings() {
		super("ProductionPlannings", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	protected DefaultProductionPlanning newElementEntity() {
		return new DefaultProductionPlanning();
	}
}