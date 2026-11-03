package mdt.model.sm.data;

import mdt.model.TopLevelEntity;

/**
 * {@code Line}은 Data 서브모델에 정의된 생산 라인 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Line extends TopLevelEntity {
	/**
	 * 라인 식별자({@code LineID})를 반환한다.
	 *
	 * @return	라인 식별자.
	 */
	public String getLineID();
	/**
	 * 라인 식별자({@code LineID})를 설정한다.
	 *
	 * @param lineID	설정할 라인 식별자.
	 */
	public void setLineID(String lineID);

	/**
	 * 라인 이름({@code LineName})을 반환한다.
	 *
	 * @return	라인 이름.
	 */
	public String getLineName();
	/**
	 * 라인 이름({@code LineName})을 설정한다.
	 *
	 * @param lineName	설정할 라인 이름.
	 */
	public void setLineName(String lineName);

	/**
	 * 라인 유형({@code LineType})을 반환한다.
	 *
	 * @return	라인 유형.
	 */
	public String getLineType();
	/**
	 * 라인 유형({@code LineType})을 설정한다.
	 *
	 * @param lineType	설정할 라인 유형.
	 */
	public void setLineType(String lineType);

	/**
	 * 라인 사용 여부 표시자({@code UseIndicator})를 반환한다.
	 *
	 * @return	라인 사용 여부 표시자.
	 */
	public String getUseIndicator();
	/**
	 * 라인 사용 여부 표시자({@code UseIndicator})를 설정한다.
	 *
	 * @param useIndicator	설정할 라인 사용 여부 표시자.
	 */
	public void setUseIndicator(String useIndicator);
	
	/**
	 * 라인 상태({@code LineStatus})를 반환한다.
	 *
	 * @return	라인 상태.
	 */
	public String getLineStatus();
	/**
	 * 라인 상태({@code LineStatus})를 설정한다.
	 *
	 * @param status	설정할 라인 상태.
	 */
	public void setLineStatus(String status);
	
//	public List<BOM> getBOMs();
//	public void setBOMs(List<BOM> boms);
	
//	public List<ItemMaster> getItemMasters();
//	public void setItemMasters(List<ItemMaster> list);
	
//	public List<Routing> getRoutings();
//	public void setRoutings(List<Routing> list);

	/**
	 * 주어진 idShort 경로에 해당하는 하위 요소의 값을 갱신한다.
	 * <p>
	 * 기본 구현은 이 기능을 지원하지 않으며 항상 {@link UnsupportedOperationException}을 발생시킨다.
	 *
	 * @param idShortPath	갱신할 하위 요소의 idShort 경로.
	 * @param value	갱신할 값.
	 * @throws UnsupportedOperationException	항상 발생된다.
	 */
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
