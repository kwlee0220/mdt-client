package mdt.model.sm.data;

/**
 * {@code ProductionPlanning}은 생산 계획 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPlanning {
	/**
	 * 생산 계획 식별자({@code ProductionPlanID})를 반환한다.
	 *
	 * @return	생산 계획 식별자.
	 */
	public String getProductionPlanID();
	/**
	 * 생산 계획 식별자({@code ProductionPlanID})를 설정한다.
	 *
	 * @param id	설정할 생산 계획 식별자.
	 */
	public void setProductionPlanID(String id);

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
	 * 생산 계획 수량({@code ProductionPlanQuantity})을 반환한다.
	 *
	 * @return	생산 계획 수량.
	 */
	public String getProductionPlanQuantity();
	/**
	 * 생산 계획 수량({@code ProductionPlanQuantity})을 설정한다.
	 *
	 * @param quantity	설정할 생산 계획 수량.
	 */
	public void setProductionPlanQuantity(String quantity);
	
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
}
