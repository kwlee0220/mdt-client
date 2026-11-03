package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 * {@link ProductionOrders} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * idShort가 {@code ProductionOrders}인 SubmodelElementList에 대응되며,
 * 각 원소는 SubmodelElementCollection으로 표현된 {@link ProductionOrder} 객체이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionOrders extends SubmodelElementListEntity<DefaultProductionOrder>
										implements ProductionOrders {
	/**
	 * idShort가 {@code ProductionOrders}이고 원소 타입이 SubmodelElementCollection인
	 * 빈 목록 객체를 생성한다.
	 */
	public DefaultProductionOrders() {
		setIdShort("ProductionOrders");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultProductionOrder newMemberEntity() {
		return new DefaultProductionOrder();
	}
}