package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link BOM} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
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
	
	@Override
	public String getIdShort() {
		return this.BOMID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getBOMID());
	}
}
