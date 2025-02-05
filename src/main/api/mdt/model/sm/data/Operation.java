package mdt.model.sm.data;

import java.util.List;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Operation extends ParameterCollection {
	public String getOperationId();
	public void setOperationId(String id);
	
	public String getOperationName();
	public void setOperationName(String name);

	public String getOperationType();
	public void setOperationType(String operationType);

	public String getUseIndicator();
	public void setUseIndicator(String useIndicator);
	
	public List<ProductionOrder> getProductionOrders();
	public void setProductionOrders(List<ProductionOrder> orders);
	
//	public String getOperationStartDateTime();
//	public void setOperationStartDateTime(String dtStr);
//	
//	public String getOperationEndDateTime();
//	public void setOperationEndDateTime(String dtStr);
//	
//	public String getEventDateTime();
//	public void setEventDateTime(String dtStr);
}
