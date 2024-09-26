package mdt.ksx9101.model;

import mdt.model.SubmodelElementEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubModelInfo extends SubmodelElementEntity {
	public String getTitle();
	public void setTitle(String title);
	
	public String getCreator();
	public void setCreator(String creator);
	
	public String getType();
	public void setType(String type);
	
	public String getFormat();
	public void setFormat(String format);

	public String getIdentifier();
	public void setIdentifier(String id);
}
