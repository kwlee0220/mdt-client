package mdt.model.sm.entity;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelEntity extends AbstractSMEContainerEntity<Submodel> {
	private String m_id;
	
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	public Submodel newSubmodel() {
		DefaultSubmodel submodel = new DefaultSubmodel.Builder()
										.id(m_id)
										.idShort(getIdShort())
										.semanticId(getSemanticId())
										.submodelElements(Lists.newArrayList())
										.build();
		updateAasModel(submodel);
		return submodel;
	}

	@Override
	public void updateFromAasModel(Submodel model) {
		Preconditions.checkArgument(model != null);
		
		setIdShort(model.getIdShort());
		setId(model.getId());
		setSemanticId(model.getSemanticId());
		
		updateFields(model.getSubmodelElements());
	}

	@Override
	public void updateAasModel(Submodel model) {
		if ( getId() != null ) model.setId(getId());
		if ( getIdShort() != null ) model.setIdShort(getIdShort());
		if ( getSemanticId() != null ) model.setSemanticId(getSemanticId());
		
		Map<String, SubmodelElement> elementMap = FStream.from(model.getSubmodelElements())
														.tagKey(SubmodelElement::getIdShort)
														.toMap();
		for ( SubmodelElement newElm: super.readSubmodelElementFromFields() ) {
			elementMap.put(newElm.getIdShort(), newElm);
		}
		model.setSubmodelElements(Lists.newArrayList(elementMap.values()));
	}
}
