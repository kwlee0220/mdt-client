package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 * {@link CompositionDependencies}의 기본 구현 엔티티 ({@code CompositionDependencies} SubmodelElementList).
 * <p>
 * 각 원소는 {@link DefaultCompositionDependency}로 표현된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionDependencies extends SubmodelElementListEntity<DefaultCompositionDependency>
												implements CompositionDependencies {
	/**
	 * idShort가 {@code CompositionDependencies}이고 원소 타입이 SubmodelElementCollection인
	 * 빈 의존 관계 목록을 생성한다.
	 */
	public DefaultCompositionDependencies() {
		setIdShort("CompositionDependencies");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultCompositionDependency newMemberEntity() {
		return new DefaultCompositionDependency();
	}
}