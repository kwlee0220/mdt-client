package mdt.model.sm.data;

/**
 * {@code EquipmentParameter}는 설비 ({@link Equipment})에 속한 파라미터의 정의를 나타낸다.
 * <p>
 * {@link #getEntityId()}는 소속 설비의 식별자 ({@link #getEquipmentId()})를 반환한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface EquipmentParameter extends Parameter {
	/**
	 * 파라미터가 속한 설비의 식별자({@code EquipmentID})를 반환한다.
	 *
	 * @return	파라미터가 속한 설비의 식별자.
	 */
	public String getEquipmentId();
	/**
	 * 파라미터가 속한 설비의 식별자({@code EquipmentID})를 설정한다.
	 *
	 * @param equipmentId	설정할 파라미터가 속한 설비의 식별자.
	 */
	public void setEquipmentId(String equipmentId);
	
	public default String getEntityId() {
		return getEquipmentId();
	}
	public default void setEntityId(String containerId) {
		setEquipmentId(containerId);
	}
}
