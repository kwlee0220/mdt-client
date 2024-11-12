package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;
import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


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
	
	@Override
	public String getIdShort() {
		return "" + this.itemID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getItemID());
	}
}
