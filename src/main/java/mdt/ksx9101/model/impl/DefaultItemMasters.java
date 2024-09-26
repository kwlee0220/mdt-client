package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ItemMaster;
import mdt.ksx9101.model.ItemMasters;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultItemMasters extends SubmodelElementListEntity<ItemMaster,DefaultItemMaster>
									implements ItemMasters {
	public DefaultItemMasters() {
		super("ItemMasters", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultItemMaster newElementEntity() {
		return new DefaultItemMaster();
	}
}