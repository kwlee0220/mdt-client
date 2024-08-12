package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionOrder {
	public String getProductionOrderID();
	public void setProductionOrderID(String id);

	public String getProductionOrderSequence();
	public void setProductionOrderSequence(String seq);
	
	public String getItemID();
	public void setItemID(String id);

	public String getItemUOMCode();
	public void setItemUOMCode(String uomCode);

	public String getProductionOrderQuantity();
	public void setProductionOrderQuantity(String quantity);

	public String getProductionDueDateTime();
	public void setProductionDueDateTime(String time);
	
	public String getScheduleStartDateTime();
	public void setScheduleStartDateTime(String time);
	
	public String getScheduleEndDateTime();
	public void setScheduleEndDateTime(String time);

	public String getSalesDocumentNumber();
	public void setSalesDocumentNumber(String number);

	public String getSalesDocumentSequence();
	public void setSalesDocumentSequence(String seq);
	
	public String getOperationID();
	public void setOperationID(String id);
}
