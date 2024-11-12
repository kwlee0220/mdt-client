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
public class DefaultRoutings extends SubmodelElementListEntity<DefaultRouting>
								implements Routings {
	public DefaultRoutings() {
		setIdShort("Routings");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultRouting newMemberEntity() {
		return new DefaultRouting();
	}
}