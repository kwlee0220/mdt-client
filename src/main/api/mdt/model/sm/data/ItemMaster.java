package mdt.model.sm.data;


/**
 * {@code ItemMaster}는 품목 기준 정보 (item master)의 한 항목을 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ItemMaster {
	/**
	 * 품목 식별자({@code ItemID})를 반환한다.
	 *
	 * @return	품목 식별자.
	 */
	public String getItemID();
	/**
	 * 품목 식별자({@code ItemID})를 설정한다.
	 *
	 * @param itemID	설정할 품목 식별자.
	 */
	public void setItemID(String itemID);

	/**
	 * 품목 유형({@code ItemType})을 반환한다.
	 *
	 * @return	품목 유형.
	 */
	public String getItemType();
	/**
	 * 품목 유형({@code ItemType})을 설정한다.
	 *
	 * @param itemType	설정할 품목 유형.
	 */
	public void setItemType(String itemType);

	/**
	 * 품목 이름({@code ItemName})을 반환한다.
	 *
	 * @return	품목 이름.
	 */
	public String getItemName();
	/**
	 * 품목 이름({@code ItemName})을 설정한다.
	 *
	 * @param itemName	설정할 품목 이름.
	 */
	public void setItemName(String itemName);

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
	 * LOT 크기({@code LotSize})를 반환한다.
	 *
	 * @return	LOT 크기.
	 */
	public String getLotSize();
	/**
	 * LOT 크기({@code LotSize})를 설정한다.
	 *
	 * @param lotSize	설정할 LOT 크기.
	 */
	public void setLotSize(String lotSize);
}
