package mdt.model.sm.data;

/**
 * {@code ProductionOrder}는 생산 오더 (작업 지시) 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionOrder {
	/**
	 * 생산 오더 식별자({@code ProductionOrderID})를 반환한다.
	 *
	 * @return	생산 오더 식별자.
	 */
	public String getProductionOrderID();
	/**
	 * 생산 오더 식별자({@code ProductionOrderID})를 설정한다.
	 *
	 * @param id	설정할 생산 오더 식별자.
	 */
	public void setProductionOrderID(String id);

	/**
	 * 생산 오더 순번({@code ProductionOrderSequence})을 반환한다.
	 *
	 * @return	생산 오더 순번.
	 */
	public String getProductionOrderSequence();
	/**
	 * 생산 오더 순번({@code ProductionOrderSequence})을 설정한다.
	 *
	 * @param seq	설정할 생산 오더 순번.
	 */
	public void setProductionOrderSequence(String seq);
	
	/**
	 * 생산 품목의 식별자({@code ItemID})를 반환한다.
	 *
	 * @return	생산 품목의 식별자.
	 */
	public String getItemID();
	/**
	 * 생산 품목의 식별자({@code ItemID})를 설정한다.
	 *
	 * @param id	설정할 생산 품목의 식별자.
	 */
	public void setItemID(String id);

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
	 * 생산 오더 수량({@code ProductionOrderQuantity})을 반환한다.
	 *
	 * @return	생산 오더 수량.
	 */
	public String getProductionOrderQuantity();
	/**
	 * 생산 오더 수량({@code ProductionOrderQuantity})을 설정한다.
	 *
	 * @param quantity	설정할 생산 오더 수량.
	 */
	public void setProductionOrderQuantity(String quantity);

	/**
	 * 생산 납기 일시({@code ProductionDueDateTime})를 반환한다.
	 *
	 * @return	생산 납기 일시.
	 */
	public String getProductionDueDateTime();
	/**
	 * 생산 납기 일시({@code ProductionDueDateTime})를 설정한다.
	 *
	 * @param time	설정할 생산 납기 일시.
	 */
	public void setProductionDueDateTime(String time);
	
	/**
	 * 계획 시작 일시({@code ScheduleStartDateTime})를 반환한다.
	 *
	 * @return	계획 시작 일시.
	 */
	public String getScheduleStartDateTime();
	/**
	 * 계획 시작 일시({@code ScheduleStartDateTime})를 설정한다.
	 *
	 * @param time	설정할 계획 시작 일시.
	 */
	public void setScheduleStartDateTime(String time);
	
	/**
	 * 계획 종료 일시({@code ScheduleEndDateTime})를 반환한다.
	 *
	 * @return	계획 종료 일시.
	 */
	public String getScheduleEndDateTime();
	/**
	 * 계획 종료 일시({@code ScheduleEndDateTime})를 설정한다.
	 *
	 * @param time	설정할 계획 종료 일시.
	 */
	public void setScheduleEndDateTime(String time);

	/**
	 * 판매 문서 번호({@code SalesDocumentNumber})를 반환한다.
	 *
	 * @return	판매 문서 번호.
	 */
	public String getSalesDocumentNumber();
	/**
	 * 판매 문서 번호({@code SalesDocumentNumber})를 설정한다.
	 *
	 * @param number	설정할 판매 문서 번호.
	 */
	public void setSalesDocumentNumber(String number);

	/**
	 * 판매 문서 순번({@code SalesDocumentSequence})을 반환한다.
	 *
	 * @return	판매 문서 순번.
	 */
	public String getSalesDocumentSequence();
	/**
	 * 판매 문서 순번({@code SalesDocumentSequence})을 설정한다.
	 *
	 * @param seq	설정할 판매 문서 순번.
	 */
	public void setSalesDocumentSequence(String seq);
	
	/**
	 * 생산 오더를 수행하는 공정의 식별자({@code OperationID})를 반환한다.
	 *
	 * @return	생산 오더를 수행하는 공정의 식별자.
	 */
	public String getOperationID();
	/**
	 * 생산 오더를 수행하는 공정의 식별자({@code OperationID})를 설정한다.
	 *
	 * @param id	설정할 생산 오더를 수행하는 공정의 식별자.
	 */
	public void setOperationID(String id);
}
