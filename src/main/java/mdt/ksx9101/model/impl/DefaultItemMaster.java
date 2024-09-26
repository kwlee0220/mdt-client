package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.ItemMaster;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class DefaultItemMaster extends SubmodelElementCollectionEntity implements ItemMaster {
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="ItemType") private String itemType;
	@PropertyField(idShort="ItemName") private String itemName;
	@PropertyField(idShort="ItemUOMCode") private String itemUOMCode;
	@PropertyField(idShort="LotSize") private String lotSize;
	
	public DefaultItemMaster() {
		super(null, "itemID");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getItemID());
	}
}
