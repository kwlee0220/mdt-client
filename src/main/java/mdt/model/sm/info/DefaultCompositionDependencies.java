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
public class DefaultCompositionDependencies extends SubmodelElementListEntity<DefaultCompositionDependency>
												implements CompositionDependencies {
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