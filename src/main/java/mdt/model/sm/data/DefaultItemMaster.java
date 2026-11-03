package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link ItemMaster} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@Setter
public class DefaultItemMaster extends SubmodelElementCollectionEntity implements ItemMaster {
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="ItemType") private String itemType;
	@PropertyField(idShort="ItemName") private String itemName;
	@PropertyField(idShort="ItemUOMCode") private String itemUOMCode;
	@PropertyField(idShort="LotSize") private String lotSize;
	
	@Override
	public String getIdShort() {
		return "" + this.itemID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getItemID());
	}
}
