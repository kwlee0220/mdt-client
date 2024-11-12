package mdt.model.sm;

import utils.func.FOption;

import okhttp3.MediaType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTFile {
	public String getPath();
	public void setPath(String path);
	
	public String getContentType();
	public void setContentType(String contentType);
	
	public byte[] getContent();
	public void setContent(byte[] content);

	public default MediaType getMediaType() {
		return FOption.map(getContentType(), MediaType::parse);
	}
}
