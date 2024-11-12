package mdt.model.sm.data;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Routing {
	public String getRoutingID();
	public void setRoutingID(String routingId);

	public String getRoutingName();
	public void setRoutingName(String routingName);

	public String getItemID();
	public void setItemID(String id);

	public String getSetupTime();
	public void setSetupTime(String time);
}
