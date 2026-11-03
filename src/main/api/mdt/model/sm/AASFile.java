package mdt.model.sm;

import okhttp3.MediaType;

import utils.Preconditions;

import mdt.model.sm.value.FileValue;

/**
 * AAS {@code File} SubmodelElement의 내용(파일 경로와 content type)을 표현하는 클래스.
 * <p>
 * {@code path}는 AAS {@code File} 요소의 {@code value}에, {@code contentType}은
 * {@code contentType}에 대응된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AASFile {
	private String m_path;
	private String m_contentType;
	
	/**
	 * 주어진 경로와 content type으로 {@link AASFile} 객체를 생성한다.
	 *
	 * @param path	파일 경로.
	 * @param contentType	파일의 content type (MIME 타입).
	 * @return	생성된 {@link AASFile} 객체.
	 */
	public static AASFile of(String path, String contentType) {
		return new AASFile(path, contentType);
	}
	
	/**
	 * 주어진 AAS {@code File} SubmodelElement의 {@code value}와 {@code contentType}으로
	 * {@link AASFile} 객체를 생성한다.
	 *
	 * @param aasFile	AAS {@code File} SubmodelElement.
	 * @return	생성된 {@link AASFile} 객체.
	 */
	public static AASFile of(org.eclipse.digitaltwin.aas4j.v3.model.File aasFile) {
		return new AASFile(aasFile.getValue(), aasFile.getContentType());
	}
	
	private AASFile() { }
	
	private AASFile(String path, String contentType) {
		m_path = path;
		m_contentType = contentType;
	}

	/**
	 * 파일 경로를 반환한다.
	 *
	 * @return	파일 경로.
	 */
	public String getPath() {
		return m_path;
	}

	/**
	 * 파일 경로를 설정한다.
	 *
	 * @param path	파일 경로.
	 * @throws IllegalArgumentException	{@code path}가 {@code null}인 경우.
	 */
	public void setPath(String path) {
		Preconditions.checkNotNullArgument(path, "path should not be null");
		
		m_path = path;
	}

	/**
	 * 파일의 content type을 반환한다.
	 *
	 * @return	content type (MIME 타입).
	 */
	public String getContentType() {
		return m_contentType;
	}

	/**
	 * 파일의 content type을 설정한다.
	 *
	 * @param contentType	content type (MIME 타입).
	 * @throws IllegalArgumentException	{@code contentType}이 {@code null}인 경우.
	 */
	public void setContentType(String contentType) {
		Preconditions.checkNotNullArgument(contentType, "contentType should not be null");
		
		m_contentType = contentType;
	}

	/**
	 * 현재 {@link AASFile}의 content type을 {@link MediaType} 객체로 반환한다.
	 *
	 * @return	{@link MediaType} 객체. content type이 {@code null}이거나
	 * 			{@link MediaType#parse(String)}가 인식하지 못하는 형식이면 {@code null}.
	 */
	public MediaType getMediaType() {
		return (getContentType() != null) ? MediaType.parse(getContentType()) : null;
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
		String pathStr = (m_path != null) ? m_path : "unknown";
		String typeStr = (m_contentType != null) ? String.format("type=%s", m_contentType) : "unknown";
		return String.format("%s(%s)", pathStr, typeStr);
	}
}
