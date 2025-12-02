package mdt.model.sm;

import com.google.common.base.Preconditions;

import okhttp3.MediaType;

import utils.func.FOption;

import mdt.model.sm.value.FileValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AASFile {
	private String m_path;
	private String m_contentType;
	
	public static AASFile of(String path, String contentType) {
		return new AASFile(path, contentType);
	}
	
	public static AASFile of(org.eclipse.digitaltwin.aas4j.v3.model.File aasFile) {
		return new AASFile(aasFile.getValue(), aasFile.getContentType());
	}
	
	private AASFile() { }
	
	private AASFile(String path, String contentType) {
		m_path = path;
		m_contentType = contentType;
	}

	public String getPath() {
		return m_path;
	}

	public void setPath(String path) {
		Preconditions.checkNotNull(path, "path should not be null");
		
		m_path = path;
	}

	public String getContentType() {
		return m_contentType;
	}

	public void setContentType(String contentType) {
		Preconditions.checkNotNull(contentType, "contentType should not be null");
		
		m_contentType = contentType;
	}

	/**
	 * 현재 {@link AASFile}의 content type을 {@link MediaType} 객체로 반환한다.
	 *
	 * @return {@link MediaType} 객체.
	 */
	public MediaType getMediaType() {
		return FOption.map(getContentType(), MediaType::parse);
	}
	
	/**
	 * 현재 {@link AASFile} 객체를 표현하는 {@link FileValue} 객체를 반환한다.
	 *
	 * @return	{@link FileValue} 객체.
	 */
	public FileValue getValue() {
		return new FileValue(getPath(), getContentType());
	}
	
	@Override
	public String toString() {
		String pathStr = FOption.getOrElse(m_path, "unknown");
		String typeStr = FOption.mapOrElse(m_contentType,
											t -> String.format("type=%s", t), "unknown");
		return String.format("%s(%s)", pathStr, typeStr);
	}
}
