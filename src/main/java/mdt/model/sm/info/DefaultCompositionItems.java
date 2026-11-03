package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 * {@link CompositionItems}의 기본 구현 엔티티 ({@code CompositionItems} SubmodelElementList).
 * <p>
 * 각 원소는 {@link DefaultCompositionItem}으로 표현된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionItems extends SubmodelElementListEntity<DefaultCompositionItem>
									implements CompositionItems {
	/**
	 * idShort가 {@code CompositionItems}인 빈 구성 항목 목록을 생성한다.
	 * <p>
	 * 원소 타입({@code typeValueListElement})은 {@code SubmodelElementCollection}으로 설정된다.
	 */
	public DefaultCompositionItems() {
		setIdShort("CompositionItems");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultCompositionItem newMemberEntity() {
		return new DefaultCompositionItem();
	}
	
	@Override
	public String toString() {
		return String.format("CompositionItems(%d)", getElementAll().size());
	}
}