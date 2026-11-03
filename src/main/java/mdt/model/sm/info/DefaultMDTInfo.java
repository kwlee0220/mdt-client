package mdt.model.sm.info;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link MDTInfo}의 기본 구현 엔티티 ({@code MDTInfo} SubmodelElementCollection).
 * <p>
 * {@code AssetName}, {@code AssetType}, {@code Status} Property를 필드로 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultMDTInfo extends SubmodelElementCollectionEntity implements MDTInfo {
	@PropertyField(idShort="AssetName") private String assetName;
	@PropertyField(idShort="AssetType") private MDTAssetType assetType;
	@PropertyField(idShort="Status") private MDTAssetStatus status;
	
	/**
	 * idShort가 {@code MDTInfo}인 빈 MDT 정보 엔티티를 생성한다.
	 */
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
