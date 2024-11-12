package mdt.model.sm.simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ModelFileVersion {
	public String getModelVersionId();
	public void setModelVersionId(String id);
	
	public String getModelPreviewImage();
	public void setModelPreviewImage(String image);
	
	public String getDigitalFile();
	public void setDigitalFile(String file);
	
	public String getModelFileReleaseNotesTxt();
	public void setModelFileReleaseNotesTxt(String file);
	
	public String getModelFileReleaseNotesFile();
	public void setModelFileReleaseNotesFile(String file);
}
