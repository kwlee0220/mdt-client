package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ItemMaster;
import mdt.ksx9101.model.ItemMasters;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultItemMasters extends SubmodelElementListHandle<ItemMaster,DefaultItemMaster>
									implements ItemMasters {
	public DefaultItemMasters() {
		super("ItemMasters", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultItemMaster newElementHandle() {
		return new DefaultItemMaster();
	}
}