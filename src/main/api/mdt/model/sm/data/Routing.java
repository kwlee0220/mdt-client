package mdt.model.sm.data;

/**
 * {@code Routing}은 품목의 공정 경로 (routing) 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Routing {
	/**
	 * 라우팅 식별자({@code RoutingID})를 반환한다.
	 *
	 * @return	라우팅 식별자.
	 */
	public String getRoutingID();
	/**
	 * 라우팅 식별자({@code RoutingID})를 설정한다.
	 *
	 * @param routingId	설정할 라우팅 식별자.
	 */
	public void setRoutingID(String routingId);

	/**
	 * 라우팅 이름({@code RoutingName})을 반환한다.
	 *
	 * @return	라우팅 이름.
	 */
	public String getRoutingName();
	/**
	 * 라우팅 이름({@code RoutingName})을 설정한다.
	 *
	 * @param routingName	설정할 라우팅 이름.
	 */
	public void setRoutingName(String routingName);

	/**
	 * 라우팅 대상 품목의 식별자({@code ItemID})를 반환한다.
	 *
	 * @return	라우팅 대상 품목의 식별자.
	 */
	public String getItemID();
	/**
	 * 라우팅 대상 품목의 식별자({@code ItemID})를 설정한다.
	 *
	 * @param id	설정할 라우팅 대상 품목의 식별자.
	 */
	public void setItemID(String id);

	/**
	 * 준비 (setup) 시간({@code SetupTime})을 반환한다.
	 *
	 * @return	준비 (setup) 시간.
	 */
	public String getSetupTime();
	/**
	 * 준비 (setup) 시간({@code SetupTime})을 설정한다.
	 *
	 * @param time	설정할 준비 (setup) 시간.
	 */
	public void setSetupTime(String time);
}
