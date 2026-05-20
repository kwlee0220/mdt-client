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
public class DefaultProductionPlannings extends SubmodelElementListEntity<DefaultProductionPlanning>
											implements ProductionPlannings {
	public DefaultProductionPlannings() {
		setIdShort("ProductionPlannings");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	protected DefaultProductionPlanning newMemberEntity() {
		return new DefaultProductionPlanning();
	}
}