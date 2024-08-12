package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;

import com.google.common.base.Preconditions;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AbstractMDTSubmodel extends AbstractMDTSubmodelElementContainer
									implements MDTAASModel<Submodel> {
	private String m_id;
	private Reference m_semanticId;
	
	protected AbstractMDTSubmodel() { }
	protected AbstractMDTSubmodel(String id) {
		super();
		
		m_id = id;
		m_semanticId = null;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	public void setSemanticId(Reference semanticId) {
		m_semanticId = semanticId;
	}
	
	@Override
	public Submodel toAasModel() {
		Preconditions.checkState(m_id != null);
		
		List<SubmodelElement> elements = super.fromFields();
		return new DefaultSubmodel.Builder()
									.id(m_id)
									.idShort(getIdShort())
									.semanticId(m_semanticId)
									.submodelElements(elements)
									.build();
	}

	@Override
	public void fromAasModel(Submodel model) {
		m_id = model.getId();
		m_idShort = model.getId();
		m_semanticId = model.getSemanticId();
		m_category = model.getCategory();
		
		toFields(model.getSubmodelElements());
	}
}
