package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.Equipments;
import mdt.model.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultEquipments extends SubmodelElementListEntity<Equipment,DefaultEquipment>
								implements Equipments {
	public DefaultEquipments() {
		super("Equipments", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultEquipment newElementEntity() {
		return new DefaultEquipment();
	}
}