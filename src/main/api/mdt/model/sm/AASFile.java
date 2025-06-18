package mdt.model.sm;

import utils.func.FOption;

import mdt.model.sm.value.FileValue;

import okhttp3.MediaType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AASFile {
	/**
	 * Returns the path of this file stored in the MDTInstance.
	 * 
	 * @return	file path.
	 */
	public String getPath();

	/**
	 * Sets the path of this file stored in the MDTInstance.
	 * 
	 * @param path file path.
	 */
	public void setPath(String path);
	
	/**
	 * Returns the content type of this file stored in the MDTInstance.
	 * 
	 * @return content type (in MIME format).
	 */
	public String getContentType();

	/**
	 * Sets the content type of this file stored in the MDTInstance.
	 * 
	 * @param contentType	MIME content type.
	 */
	public void setContentType(String contentType);
	
	/**
	 * Returns the content of this file stored in the MDTInstance.
	 * 
	 * @return file content.
	 */
	public byte[] getContent();
	
	/**
	 * Sets the content of this file stored in the MDTInstance.
	 * 
	 * @param content file content.
	 */
	public void setContent(byte[] content);

	/**
	 * Returns the media type of this file stored in the MDTInstance.
	 * 
	 * @return media type.
	 */
	public default MediaType getMediaType() {
		return FOption.map(getContentType(), MediaType::parse);
	}
	
	public default FileValue getValue() {
		return new FileValue(getContentType(), getPath());
	}
}
