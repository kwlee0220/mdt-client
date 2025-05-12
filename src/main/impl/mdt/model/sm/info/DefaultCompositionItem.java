package mdt.model.sm.info;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionItem extends SubmodelElementCollectionEntity
										implements CompositionItem {
	@PropertyField(idShort="ID") private String ID;
	@PropertyField(idShort="Reference") private String reference;
	@PropertyField(idShort="Description") private String description;
	
	@Override
	public String getIdShort() {
		return ID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getID());
	}
}
