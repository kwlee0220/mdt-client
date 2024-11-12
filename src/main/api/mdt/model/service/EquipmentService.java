package mdt.model.service;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.data.DefaultEquipment;
import mdt.model.sm.entity.SMCollectionField;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class EquipmentService extends ParameterCollectionBase {
	@SMCollectionField(idShort="Equipment") private DefaultEquipment equipment;
	
	public EquipmentService(SubmodelService service) {
		super(service, "Equipment");
	}
}
