package mdt.model.sm.data;


/**
 * {@code OperationParameterValue}는 공정 ({@link Operation})에 속한 파라미터의 값을 나타낸다.
 * <p>
 * {@link #getEntityId()}는 소속 공정의 식별자 ({@link #getOperationId()})를 반환한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface OperationParameterValue extends ParameterValue {
	/**
	 * 파라미터 값이 속한 공정의 식별자({@code OperationID})를 반환한다.
	 *
	 * @return	파라미터 값이 속한 공정의 식별자.
	 */
	public String getOperationId();
	/**
	 * 파라미터 값이 속한 공정의 식별자({@code OperationID})를 설정한다.
	 *
	 * @param operationId	설정할 파라미터 값이 속한 공정의 식별자.
	 */
	public void setOperationId(String operationId);
	
	public default String getEntityId() {
		return getOperationId();
	}
	public default void setEntityId(String containerId) {
		setOperationId(containerId);
	}
}
