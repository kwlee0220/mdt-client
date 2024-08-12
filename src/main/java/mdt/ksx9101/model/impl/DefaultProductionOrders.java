package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ProductionOrder;
import mdt.ksx9101.model.ProductionOrders;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionOrders
							extends SubmodelElementListHandle<ProductionOrder, DefaultProductionOrder>
							implements ProductionOrders {
	public DefaultProductionOrders() {
		super("ProductionOrders", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultProductionOrder newElementHandle() {
		return new DefaultProductionOrder();
	}
}