package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.CompositionItem;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionItem extends SubmodelElementCollectionEntity
										implements CompositionItem {
	@PropertyField(idShort="ID") private String id;
	@PropertyField(idShort="Reference") private String reference;
	
	public DefaultCompositionItem() {
		super(null, "ID");
	}

	@Override
	public String getID() {
		return getIdShort();
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public String getReference() {
		return this.reference;
	}

	@Override
	public void setReference(String aasId) {
		this.reference = aasId;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getID());
	}
}
