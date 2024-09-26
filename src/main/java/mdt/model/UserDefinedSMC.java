package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class UserDefinedSMC extends UserDefinedSubmodelElementContainer implements UserDefinedSubmodelElement {
	protected Reference m_semanticId;
	
	protected UserDefinedSMC(String idShort, String idShortRef) {
		super(idShort, idShortRef);
	}
	protected UserDefinedSMC() {
		this(null, null);
	}
	
	public void setSemanticId(Reference semanticId) {
		m_semanticId = semanticId;
	}
	
	@Override
	public SubmodelElementCollection toAasModel() {
		List<SubmodelElement> submodelElements = super.fromFields();
		return new DefaultSubmodelElementCollection.Builder()
													.idShort(getIdShort())
													.semanticId(m_semanticId)
													.category(m_category)
													.value(submodelElements)
													.build();
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		toFields(smc.getValue());
	}
}
