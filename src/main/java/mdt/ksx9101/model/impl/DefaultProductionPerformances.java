package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ProductionPerformance;
import mdt.ksx9101.model.ProductionPerformances;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionPerformances
				extends SubmodelElementListHandle<ProductionPerformance,DefaultProductionPerformance>
				implements ProductionPerformances {
	public DefaultProductionPerformances() {
		super("ProductionPerformances", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultProductionPerformance newElementHandle() {
		return new DefaultProductionPerformance();
	}
}