package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface BOM {
	public String getBOMID();
	public void setBOMID(String bomID);

	public String getBOMType();
	public void setBOMType(String bomType);

	public String getItemID();
	public void setItemID(String itemID);

	public String getBOMQuantity();
	public void setBOMQuantity(String quantity);

	public String getItemUOMCode();
	public void setItemUOMCode(String uomCode);
	
	public String getValidStartDateTime();
	public void setValidStartDateTime(String time);
	
	public String getValidEndDateTime();
	public void setValidEndDateTime(String time);
}
