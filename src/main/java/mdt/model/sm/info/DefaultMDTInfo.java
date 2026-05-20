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
public class DefaultMDTInfo extends SubmodelElementCollectionEntity implements MDTInfo {
	@PropertyField(idShort="AssetName") private String assetName;
	@PropertyField(idShort="AssetType") private MDTAssetType assetType;
	@PropertyField(idShort="Status") private MDTAssetStatus status;
	@PropertyField(idShort="IdShort") private String idShort;
	
	public DefaultMDTInfo() {
		setIdShort("MDTInfo");
	}
	
	@Override
	public String toString() {
		return String.format("%s[IdShort=%s, AssetName=%s, AssetType=%s, Status=%s]",
							getClass().getSimpleName(), getIdShort(), getAssetName(),
							getAssetType(), getStatus());
	}
}
