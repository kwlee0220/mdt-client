package mdt.model.sm.data;

/**
 * {@code ProductionPerformance}는 생산 실적 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPerformance {
	/**
	 * 생산 실적 식별자({@code ProductionPerformanceID})를 반환한다.
	 *
	 * @return	생산 실적 식별자.
	 */
	public String getProductionPerformanceID();
	/**
	 * 생산 실적 식별자({@code ProductionPerformanceID})를 설정한다.
	 *
	 * @param productionPerformanceID	설정할 생산 실적 식별자.
	 */
	public void setProductionPerformanceID(String productionPerformanceID);
	
	/**
	 * 생산 실적 순번({@code ProductionPerformanceSequence})을 반환한다.
	 *
	 * @return	생산 실적 순번.
	 */
	public String getProductionPerformanceSequence();
	/**
	 * 생산 실적 순번({@code ProductionPerformanceSequence})을 설정한다.
	 *
	 * @param productionPerformanceSequence	설정할 생산 실적 순번.
	 */
	public void setProductionPerformanceSequence(String productionPerformanceSequence);

	/**
	 * 관련 생산 오더의 식별자({@code ProductionOrderID})를 반환한다.
	 *
	 * @return	관련 생산 오더의 식별자.
	 */
	public String getProductionOrderID();
	/**
	 * 관련 생산 오더의 식별자({@code ProductionOrderID})를 설정한다.
	 *
	 * @param productionOrderID	설정할 관련 생산 오더의 식별자.
	 */
	public void setProductionOrderID(String productionOrderID);

	/**
	 * 관련 생산 오더의 순번({@code ProductionOrderSequence})을 반환한다.
	 *
	 * @return	관련 생산 오더의 순번.
	 */
	public String getProductionOrderSequence();
	/**
	 * 관련 생산 오더의 순번({@code ProductionOrderSequence})을 설정한다.
	 *
	 * @param seq	설정할 관련 생산 오더의 순번.
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
	 * @param itemID	설정할 생산 품목의 식별자.
	 */
	public void setItemID(String itemID);

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
	 * 생산 수량({@code ProducedQuantity})을 반환한다.
	 *
	 * @return	생산 수량.
	 */
	public String getProducedQuantity();
	/**
	 * 생산 수량({@code ProducedQuantity})을 설정한다.
	 *
	 * @param quantity	설정할 생산 수량.
	 */
	public void setProducedQuantity(String quantity);

	/**
	 * 불량 수량({@code DefectQuantity})을 반환한다.
	 *
	 * @return	불량 수량.
	 */
	public String getDefectQuantity();
	/**
	 * 불량 수량({@code DefectQuantity})을 설정한다.
	 *
	 * @param quantity	설정할 불량 수량.
	 */
	public void setDefectQuantity(String quantity);

	/**
	 * 실적이 발생한 공정의 식별자({@code OperationID})를 반환한다.
	 *
	 * @return	실적이 발생한 공정의 식별자.
	 */
	public String getOperationID();
	/**
	 * 실적이 발생한 공정의 식별자({@code OperationID})를 설정한다.
	 *
	 * @param operationID	설정할 실적이 발생한 공정의 식별자.
	 */
	public void setOperationID(String operationID);

	/**
	 * 공정 순번({@code OperationSequence})을 반환한다.
	 *
	 * @return	공정 순번.
	 */
	public String getOperationSequence();
	/**
	 * 공정 순번({@code OperationSequence})을 설정한다.
	 *
	 * @param operationSequence	설정할 공정 순번.
	 */
	public void setOperationSequence(String operationSequence);
	
	/**
	 * 실행 시작 일시({@code ExecutionStartDateTime})를 반환한다.
	 *
	 * @return	실행 시작 일시.
	 */
	public String getExecutionStartDateTime();
	/**
	 * 실행 시작 일시({@code ExecutionStartDateTime})를 설정한다.
	 *
	 * @param time	설정할 실행 시작 일시.
	 */
	public void setExecutionStartDateTime(String time);
	
	/**
	 * 실행 종료 일시({@code ExecutionEndDateTime})를 반환한다.
	 *
	 * @return	실행 종료 일시.
	 */
	public String getExecutionEndDateTime();
	/**
	 * 실행 종료 일시({@code ExecutionEndDateTime})를 설정한다.
	 *
	 * @param time	설정할 실행 종료 일시.
	 */
	public void setExecutionEndDateTime(String time);

	/**
	 * LOT 식별자({@code LotID})를 반환한다.
	 *
	 * @return	LOT 식별자.
	 */
	public String getLotID();
	/**
	 * LOT 식별자({@code LotID})를 설정한다.
	 *
	 * @param lotID	설정할 LOT 식별자.
	 */
	public void setLotID(String lotID);
}
