package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 * {@link ItemMasters} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * idShort가 {@code ItemMasters}인 SubmodelElementList에 대응되며, 각 원소는 SubmodelElementCollection으로
 * 표현된 {@link ItemMaster} 객체이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultItemMasters extends SubmodelElementListEntity<DefaultItemMaster>
									implements ItemMasters {
	/**
	 * idShort가 {@code ItemMasters}이고 원소 타입이 SubmodelElementCollection인 빈 목록 객체를 생성한다.
	 */
	public DefaultItemMasters() {
		setIdShort("ItemMasters");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultItemMaster newMemberEntity() {
		return new DefaultItemMaster();
	}
}