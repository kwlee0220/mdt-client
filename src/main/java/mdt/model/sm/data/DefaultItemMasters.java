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
public class DefaultItemMasters extends SubmodelElementListEntity<DefaultItemMaster>
									implements ItemMasters {
	public DefaultItemMasters() {
		setIdShort("ItemMasters");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultItemMaster newMemberEntity() {
		return new DefaultItemMaster();
	}
}