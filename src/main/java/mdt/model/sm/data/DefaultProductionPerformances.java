package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 * {@link ProductionPerformances} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * idShort가 {@code ProductionPerformances}인 SubmodelElementList에 대응되며,
 * 각 원소는 SubmodelElementCollection으로 표현된 {@link ProductionPerformance} 객체이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultProductionPerformances extends SubmodelElementListEntity<DefaultProductionPerformance>
											implements ProductionPerformances {
	/**
	 * idShort가 {@code ProductionPerformances}이고 원소 타입이 SubmodelElementCollection인
	 * 빈 목록 객체를 생성한다.
	 */
	public DefaultProductionPerformances() {
		setIdShort("ProductionPerformances");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultProductionPerformance newMemberEntity() {
		return new DefaultProductionPerformance();
	}
}