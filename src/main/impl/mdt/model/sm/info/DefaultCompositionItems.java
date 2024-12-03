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
public class DefaultCompositionItems extends SubmodelElementListEntity<DefaultComponentItem>
									implements CompositionItems {
	public DefaultCompositionItems() {
		setIdShort("CompositionItems");
		setOrderRelevant(false);
		setTypeValueListElement(AasSubmodelElements.REFERENCE_ELEMENT);
	}

	@Override
	public DefaultComponentItem newMemberEntity() {
		return new DefaultComponentItem();
	}
	
	@Override
	public String toString() {
		return String.format("CompositionItems(%d)", getClass().getSimpleName(),
								getMemberList().size());
	}
}