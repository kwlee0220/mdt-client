package mdt.model.sm.data;

import java.sql.Timestamp;

/**
 * {@code LOT}은 설비에서 작업 중인 생산 LOT 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface LOT {
	/**
	 * LOT 식별자({@code LotId})를 반환한다.
	 *
	 * @return	LOT 식별자.
	 */
	public String getLotId();
	/**
	 * LOT 식별자({@code LotId})를 설정한다.
	 *
	 * @param lotId	설정할 LOT 식별자.
	 */
	public void setLotId(String lotId);

	/**
	 * LOT의 품목 식별자({@code ItemId})를 반환한다.
	 *
	 * @return	LOT의 품목 식별자.
	 */
	public String getItemId();
	/**
	 * LOT의 품목 식별자({@code ItemId})를 설정한다.
	 *
	 * @param id	설정할 LOT의 품목 식별자.
	 */
	public void setItemId(String id);
	
	/**
	 * LOT 상태({@code State})를 반환한다.
	 *
	 * @return	LOT 상태.
	 */
	public String getState();
	/**
	 * LOT 상태({@code State})를 설정한다.
	 *
	 * @param state	설정할 LOT 상태.
	 */
	public void setState(String state);
	
	/**
	 * LOT 수량({@code Quantity})을 반환한다.
	 *
	 * @return	LOT 수량.
	 */
	public Integer getQuantity();
	/**
	 * LOT 수량({@code Quantity})을 설정한다.
	 *
	 * @param quantity	설정할 LOT 수량.
	 */
	public void setQuantity(Integer quantity);
	
	/**
	 * LOT를 처리하는 공정의 식별자({@code OperationId})를 반환한다.
	 *
	 * @return	LOT를 처리하는 공정의 식별자.
	 */
	public String getOperationId();
	/**
	 * LOT를 처리하는 공정의 식별자({@code OperationId})를 설정한다.
	 *
	 * @param id	설정할 LOT를 처리하는 공정의 식별자.
	 */
	public void setOperationId(String id);
	
	/**
	 * LOT를 처리하는 설비의 식별자({@code EquipmentId})를 반환한다.
	 *
	 * @return	LOT를 처리하는 설비의 식별자.
	 */
	public String getEquipmentId();
	/**
	 * LOT를 처리하는 설비의 식별자({@code EquipmentId})를 설정한다.
	 *
	 * @param id	설정할 LOT를 처리하는 설비의 식별자.
	 */
	public void setEquipmentId(String id);
	
	/**
	 * LOT 작업 시작 시각({@code StartDateTime})을 반환한다.
	 *
	 * @return	LOT 작업 시작 시각.
	 */
	public Timestamp getStartDateTime();
	/**
	 * LOT 작업 시작 시각({@code StartDateTime})을 설정한다.
	 *
	 * @param dt	설정할 LOT 작업 시작 시각.
	 */
	public void setStartDateTime(Timestamp dt);
	
	/**
	 * LOT 작업 종료 시각({@code EndDateTime})을 반환한다.
	 *
	 * @return	LOT 작업 종료 시각.
	 */
	public Timestamp getEndDateTime();
	/**
	 * LOT 작업 종료 시각({@code EndDateTime})을 설정한다.
	 *
	 * @param dt	설정할 LOT 작업 종료 시각.
	 */
	public void setEndDateTime(Timestamp dt);
	
	/**
	 * 적용된 택트 타임({@code AppliedTactTime})을 반환한다.
	 *
	 * @return	적용된 택트 타임.
	 */
	public Float getAppliedTactTime();
	/**
	 * 적용된 택트 타임({@code AppliedTactTime})을 설정한다.
	 *
	 * @param tactTime	설정할 적용된 택트 타임.
	 */
	public void setAppliedTactTime(Float tactTime);
}
