package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionOrders extends SubmodelElementListEntity<DefaultProductionOrder>
										implements ProductionOrders {
	public DefaultProductionOrders() {
		setIdShort("ProductionOrders");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultProductionOrder newMemberEntity() {
		return new DefaultProductionOrder();
	}
}