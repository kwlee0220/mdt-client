package mdt.model;

/**
 * AI/Simulation 등의 서브모델이 참조하는 모델(또는 서브모델)의 메타 정보를 표현하는 인터페이스이다.
 * <p>
 * 제목, 작성자, 타입, 형식, 식별자로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubModelInfo {
	/**
	 * 모델의 제목을(를) 반환한다.
	 *
	 * @return	제목.
	 */
	public String getTitle();
	/**
	 * 모델의 제목을(를) 설정한다.
	 *
	 * @param title	제목.
	 */
	public void setTitle(String title);
	
	/**
	 * 모델의 작성자을(를) 반환한다.
	 *
	 * @return	작성자.
	 */
	public String getCreator();
	/**
	 * 모델의 작성자을(를) 설정한다.
	 *
	 * @param creator	작성자.
	 */
	public void setCreator(String creator);
	
	/**
	 * 모델의 타입을(를) 반환한다.
	 *
	 * @return	타입.
	 */
	public String getType();
	/**
	 * 모델의 타입을(를) 설정한다.
	 *
	 * @param type	타입.
	 */
	public void setType(String type);
	
	/**
	 * 모델의 형식을(를) 반환한다.
	 *
	 * @return	형식.
	 */
	public String getFormat();
	/**
	 * 모델의 형식을(를) 설정한다.
	 *
	 * @param format	형식.
	 */
	public void setFormat(String format);

	/**
	 * 모델의 식별자을(를) 반환한다.
	 *
	 * @return	식별자.
	 */
	public String getIdentifier();
	/**
	 * 모델의 식별자을(를) 설정한다.
	 *
	 * @param id	식별자.
	 */
	public void setIdentifier(String id);
}
