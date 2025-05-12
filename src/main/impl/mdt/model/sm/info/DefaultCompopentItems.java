package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SubmodelElementListEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompopentItems extends SubmodelElementListEntity<DefaultCompositionItem>
									implements CompositionItems {
	public DefaultCompopentItems() {
		setIdShort("CompositionItems");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.REFERENCE_ELEMENT);
	}

	@Override
	public DefaultCompositionItem newMemberEntity() {
		return new DefaultCompositionItem();
	}
	
	@Override
	public String toString() {
		return String.format("CompositionItems(%d)", getClass().getSimpleName(),
								getElementAll().size());
	}
}