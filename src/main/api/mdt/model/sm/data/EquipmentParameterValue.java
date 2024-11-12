package mdt.model.sm.data;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface EquipmentParameterValue extends ParameterValue {
	public String getEquipmentId();
	public void setEquipmentId(String equipmentId);
	
	public default String getEntityId() {
		return getEquipmentId();
	}
	public default void setEntityId(String containerId) {
		setEquipmentId(containerId);
	}
}
