package mdt.model.sm.data;

/**
 * {@code BOM}은 품목 구성 정보 (Bill of Materials)의 한 항목을 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface BOM {
	/**
	 * BOM 식별자({@code BOMID})를 반환한다.
	 *
	 * @return	BOM 식별자.
	 */
	public String getBOMID();
	/**
	 * BOM 식별자({@code BOMID})를 설정한다.
	 *
	 * @param bomID	설정할 BOM 식별자.
	 */
	public void setBOMID(String bomID);

	/**
	 * BOM 유형({@code BOMType})을 반환한다.
	 *
	 * @return	BOM 유형.
	 */
	public String getBOMType();
	/**
	 * BOM 유형({@code BOMType})을 설정한다.
	 *
	 * @param bomType	설정할 BOM 유형.
	 */
	public void setBOMType(String bomType);

	/**
	 * 구성 품목의 식별자({@code ItemID})를 반환한다.
	 *
	 * @return	구성 품목의 식별자.
	 */
	public String getItemID();
	/**
	 * 구성 품목의 식별자({@code ItemID})를 설정한다.
	 *
	 * @param itemID	설정할 구성 품목의 식별자.
	 */
	public void setItemID(String itemID);

	/**
	 * 구성 품목의 소요 수량({@code BOMQuantity})을 반환한다.
	 *
	 * @return	구성 품목의 소요 수량.
	 */
	public String getBOMQuantity();
	/**
	 * 구성 품목의 소요 수량({@code BOMQuantity})을 설정한다.
	 *
	 * @param quantity	설정할 구성 품목의 소요 수량.
	 */
	public void setBOMQuantity(String quantity);

	/**
	 * 품목의 측정 단위 코드({@code ItemUOMCode})를 반환한다.
	 *
	 * @return	품목의 측정 단위 코드.
	 */
	public String getItemUOMCode();
	/**
	 * 품목의 측정 단위 코드({@code ItemUOMCode})를 설정한다.
	 *
	 * @param uomCode	설정할 품목의 측정 단위 코드.
	 */
	public void setItemUOMCode(String uomCode);
	
	/**
	 * BOM 유효 시작 일시({@code ValidStartDateTime})를 반환한다.
	 *
	 * @return	BOM 유효 시작 일시.
	 */
	public String getValidStartDateTime();
	/**
	 * BOM 유효 시작 일시({@code ValidStartDateTime})를 설정한다.
	 *
	 * @param time	설정할 BOM 유효 시작 일시.
	 */
	public void setValidStartDateTime(String time);
	
	/**
	 * BOM 유효 종료 일시({@code ValidEndDateTime})를 반환한다.
	 *
	 * @return	BOM 유효 종료 일시.
	 */
	public String getValidEndDateTime();
	/**
	 * BOM 유효 종료 일시({@code ValidEndDateTime})를 설정한다.
	 *
	 * @param time	설정할 BOM 유효 종료 일시.
	 */
	public void setValidEndDateTime(String time);
}
