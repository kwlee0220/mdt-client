package mdt.model.sm.info;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInfo {
	public String getAssetID();
	public void setAssetID(String id);
	
	public String getAssetType();
	public void setAssetType(String type);
	
	public String getAssetName();
	public void setAssetName(String name);

	public String getStatus();
	public void setStatus(String status);

	public String getIdShort();
	public void setIdShort(String idShort);
}
