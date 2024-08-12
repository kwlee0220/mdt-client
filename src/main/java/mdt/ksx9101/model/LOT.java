package mdt.ksx9101.model;

import java.sql.Timestamp;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface LOT {
	public String getLotId();
	public void setLotId(String lotId);

	public String getItemId();
	public void setItemId(String id);
	
	public String getState();
	public void setState(String state);
	
	public Integer getQuantity();
	public void setQuantity(Integer quantity);
	
	public String getOperationId();
	public void setOperationId(String id);
	
	public String getEquipmentId();
	public void setEquipmentId(String id);
	
	public Timestamp getStartDateTime();
	public void setStartDateTime(Timestamp dt);
	
	public Timestamp getEndDateTime();
	public void setEndDateTime(Timestamp dt);
	
	public Float getAppliedTactTime();
	public void setAppliedTactTime(Float tactTime);
}
