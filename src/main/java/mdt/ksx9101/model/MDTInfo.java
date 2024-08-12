package mdt.ksx9101.model;

import mdt.model.MDTSubmodelElement;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInfo extends MDTSubmodelElement {
	public String getAssetID();
	public void setAssetID(String id);
	
	public String getAssetType();
	public void setAssetType(String type);
	
	public String getAssetName();
	public void setAssetName(String name);

	public String getStatus();
	public void setStatus(String status);
}
