package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.MDTInfo;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultMDTInfo extends AbstractMDTSubmodelElementCollection implements MDTInfo {
	@PropertyField(idShort="AssetID") private String assetID;
	@PropertyField(idShort="AssetName") private String assetName;
	@PropertyField(idShort="AssetType") private String assetType;
	@PropertyField(idShort="Status") private String status;
	
	public DefaultMDTInfo() {
		super("MDTInfo", null);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getAssetID());
	}
}
