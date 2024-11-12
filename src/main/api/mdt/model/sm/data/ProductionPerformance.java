package mdt.model.sm.data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPerformance {
	public String getProductionPerformanceID();
	public void setProductionPerformanceID(String itemID);
	
	public String getProductionPerformanceSequence();
	public void setProductionPerformanceSequence(String itemID);

	public String getProductionOrderID();
	public void setProductionOrderID(String lotSize);

	public String getProductionOrderSequence();
	public void setProductionOrderSequence(String seq);
	
	public String getItemID();
	public void setItemID(String itemID);

	public String getItemUOMCode();
	public void setItemUOMCode(String uomCode);

	public String getProducedQuantity();
	public void setProducedQuantity(String quantity);

	public String getDefectQuantity();
	public void setDefectQuantity(String quantity);

	public String getOperationID();
	public void setOperationID(String lotSize);

	public String getOperationSequence();
	public void setOperationSequence(String lotSize);
	
	public String getExecutionStartDateTime();
	public void setExecutionStartDateTime(String time);
	
	public String getExecutionEndDateTime();
	public void setExecutionEndDateTime(String time);

	public String getLotID();
	public void setLotID(String lotSize);
}
