package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.BOM;
import mdt.model.SubmodelElementCollectionEntity;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultBOM extends SubmodelElementCollectionEntity implements BOM {
	@PropertyField(idShort="BOMID") private String BOMID;
	@PropertyField(idShort="BOMType") private String BOMType;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="BOMQuantity") private String BOMQuantity;
	@PropertyField(idShort="ItemUOMCode") private String itemUOMCode;
	@PropertyField(idShort="ValidStartDateTime") private String validStartDateTime;
	@PropertyField(idShort="ValidEndDateTime") private String validEndDateTime;
	
	public DefaultBOM() {
		super(null, "BOMID");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getBOMID());
	}
}
