package mdt.model.sm.data;

/**
 * {@code Andon}은 Data 서브모델에 기록되는 안돈 (라인/설비 이상 알림) 정보를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Andon {
	/**
	 * 안돈 식별자({@code AndonID})를 반환한다.
	 *
	 * @return	안돈 식별자.
	 */
	public Long getAndonID();
	/**
	 * 안돈 식별자({@code AndonID})를 설정한다.
	 *
	 * @param andonID	설정할 안돈 식별자.
	 */
	public void setAndonID(Long andonID);

	/**
	 * 그룹 식별자({@code GroupID})를 반환한다.
	 *
	 * @return	그룹 식별자.
	 */
	public String getGroupID();
	/**
	 * 그룹 식별자({@code GroupID})를 설정한다.
	 *
	 * @param groupID	설정할 그룹 식별자.
	 */
	public void setGroupID(String groupID);

	/**
	 * 안돈이 발생한 공정의 식별자({@code OperationID})를 반환한다.
	 *
	 * @return	안돈이 발생한 공정의 식별자.
	 */
	public String getOperationID();
	/**
	 * 안돈이 발생한 공정의 식별자({@code OperationID})를 설정한다.
	 *
	 * @param operationID	설정할 안돈이 발생한 공정의 식별자.
	 */
	public void setOperationID(String operationID);

	/**
	 * 안돈 시작 일시({@code StartDateTime})를 반환한다.
	 *
	 * @return	안돈 시작 일시.
	 */
	public String getStartDateTime();
	/**
	 * 안돈 시작 일시({@code StartDateTime})를 설정한다.
	 *
	 * @param startDateTime	설정할 안돈 시작 일시.
	 */
	public void setStartDateTime(String startDateTime);

	/**
	 * 안돈 원인 번호({@code CauseNO})를 반환한다.
	 *
	 * @return	안돈 원인 번호.
	 */
	public String getCauseNO();
	/**
	 * 안돈 원인 번호({@code CauseNO})를 설정한다.
	 *
	 * @param causeNO	설정할 안돈 원인 번호.
	 */
	public void setCauseNO(String causeNO);

	/**
	 * 안돈 원인 이름({@code CauseName})을 반환한다.
	 *
	 * @return	안돈 원인 이름.
	 */
	public String getCauseName();
	/**
	 * 안돈 원인 이름({@code CauseName})을 설정한다.
	 *
	 * @param causeName	설정할 안돈 원인 이름.
	 */
	public void setCauseName(String causeName);

	/**
	 * 라인 정지 유형({@code LineStopType})을 반환한다.
	 *
	 * @return	라인 정지 유형.
	 */
	public String getLineStopType();
	/**
	 * 라인 정지 유형({@code LineStopType})을 설정한다.
	 *
	 * @param lineStopType	설정할 라인 정지 유형.
	 */
	public void setLineStopType(String lineStopType);

	/**
	 * 라인 정지 유형 이름({@code LineStopName})을 반환한다.
	 *
	 * @return	라인 정지 유형 이름.
	 */
	public String getLineStopName();
	/**
	 * 라인 정지 유형 이름({@code LineStopName})을 설정한다.
	 *
	 * @param lineStopName	설정할 라인 정지 유형 이름.
	 */
	public void setLineStopName(String lineStopName);

	/**
	 * 안돈 유형 코드({@code TypeCode})를 반환한다.
	 *
	 * @return	안돈 유형 코드.
	 */
	public String getTypeCode();
	/**
	 * 안돈 유형 코드({@code TypeCode})를 설정한다.
	 *
	 * @param typeCode	설정할 안돈 유형 코드.
	 */
	public void setTypeCode(String typeCode);

	/**
	 * 안돈 유형 이름({@code TypeName})을 반환한다.
	 *
	 * @return	안돈 유형 이름.
	 */
	public String getTypeName();
	/**
	 * 안돈 유형 이름({@code TypeName})을 설정한다.
	 *
	 * @param typeName	설정할 안돈 유형 이름.
	 */
	public void setTypeName(String typeName);

	/**
	 * 안돈 종료 일시({@code EndDateTime})를 반환한다.
	 *
	 * @return	안돈 종료 일시.
	 */
	public String getEndDateTime();
	/**
	 * 안돈 종료 일시({@code EndDateTime})를 설정한다.
	 *
	 * @param endDateTime	설정할 안돈 종료 일시.
	 */
	public void setEndDateTime(String endDateTime);

//	public String getStopDateTime();
//	public void setStopDateTime(String stopDateTime);
}
