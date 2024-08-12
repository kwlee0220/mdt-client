package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPlanning {
	public String getProductionPlanID();
	public void setProductionPlanID(String id);

	public String getItemID();
	public void setItemID(String id);

	public String getProductionPlanQuantity();
	public void setProductionPlanQuantity(String quantity);
	
	public String getScheduleStartDateTime();
	public void setScheduleStartDateTime(String time);
	
	public String getScheduleEndDateTime();
	public void setScheduleEndDateTime(String time);
}
