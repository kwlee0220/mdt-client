package mdt.model.sm.data;

import mdt.model.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Line extends TopLevelEntity {
	public String getLineID();
	public void setLineID(String lineID);

	public String getLineName();
	public void setLineName(String lineName);

	public String getLineType();
	public void setLineType(String lineType);

	public String getUseIndicator();
	public void setUseIndicator(String useIndicator);
	
	public String getLineStatus();
	public void setLineStatus(String status);
	
//	public List<BOM> getBOMs();
//	public void setBOMs(List<BOM> boms);
	
//	public List<ItemMaster> getItemMasters();
//	public void setItemMasters(List<ItemMaster> list);
	
//	public List<Routing> getRoutings();
//	public void setRoutings(List<Routing> list);

	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
