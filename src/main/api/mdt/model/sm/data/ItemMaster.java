package mdt.model.sm.data;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ItemMaster {
	public String getItemID();
	public void setItemID(String itemID);

	public String getItemType();
	public void setItemType(String itemType);

	public String getItemName();
	public void setItemName(String itemName);

	public String getItemUOMCode();
	public void setItemUOMCode(String uomCode);

	public String getLotSize();
	public void setLotSize(String lotSize);
}
