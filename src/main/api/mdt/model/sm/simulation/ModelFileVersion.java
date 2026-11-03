package mdt.model.sm.simulation;

/**
 * 시뮬레이션 모델 파일의 버전 정보({@code ModelFileVersion} SubmodelElementCollection) 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ModelFileVersion {
	/**
	 * 모델 버전 식별자를 반환한다.
	 *
	 * @return	모델 버전 식별자.
	 */
	public String getModelVersionId();
	/**
	 * 모델 버전 식별자를 설정한다.
	 *
	 * @param id	모델 버전 식별자.
	 */
	public void setModelVersionId(String id);
	
	/**
	 * 모델 미리보기 이미지를 반환한다.
	 *
	 * @return	모델 미리보기 이미지.
	 */
	public String getModelPreviewImage();
	/**
	 * 모델 미리보기 이미지를 설정한다.
	 *
	 * @param image	모델 미리보기 이미지.
	 */
	public void setModelPreviewImage(String image);
	
	/**
	 * 모델 디지털 파일을 반환한다.
	 *
	 * @return	모델 디지털 파일.
	 */
	public String getDigitalFile();
	/**
	 * 모델 디지털 파일을 설정한다.
	 *
	 * @param file	모델 디지털 파일.
	 */
	public void setDigitalFile(String file);
	
	/**
	 * 모델 파일 릴리스 노트 텍스트를 반환한다.
	 *
	 * @return	모델 파일 릴리스 노트 텍스트.
	 */
	public String getModelFileReleaseNotesTxt();
	/**
	 * 모델 파일 릴리스 노트 텍스트를 설정한다.
	 *
	 * @param file	모델 파일 릴리스 노트 텍스트.
	 */
	public void setModelFileReleaseNotesTxt(String file);
	
	/**
	 * 모델 파일 릴리스 노트 파일을 반환한다.
	 *
	 * @return	모델 파일 릴리스 노트 파일.
	 */
	public String getModelFileReleaseNotesFile();
	/**
	 * 모델 파일 릴리스 노트 파일을 설정한다.
	 *
	 * @param file	모델 파일 릴리스 노트 파일.
	 */
	public void setModelFileReleaseNotesFile(String file);
}
