package mdt.model.service;

import mdt.model.sm.data.DefaultEquipment;
import mdt.model.sm.entity.SMCollectionField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class EquipmentService extends ParameterCollectionBase {
	@SMCollectionField(idShort="Equipment") private DefaultEquipment m_equipment;
	
	public EquipmentService(SubmodelService service) {
		super(service, "Equipment");
	}

	public DefaultEquipment getEquipment() {
		return m_equipment;
	}

	public void setEquipment(DefaultEquipment equipment) {
		this.m_equipment = equipment;
	}

}
