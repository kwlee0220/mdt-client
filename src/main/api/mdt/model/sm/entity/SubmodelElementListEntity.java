package mdt.model.sm.entity;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SubmodelElementListEntity<E extends SubmodelElementEntity>
														extends AbstractSMEContainerEntity<SubmodelElement>
														implements SubmodelElementEntity {
	private boolean m_orderRelevant = false;
	private AasSubmodelElements m_typeValueListElement = AasSubmodelElements.SUBMODEL_ELEMENT;
	private DataTypeDefXsd m_valueTypeListElement;
	private List<E> m_members = Lists.newArrayList();
	
	protected abstract E newMemberEntity();
	
	public void setOrderRelevant(boolean orderRelevant) {
		m_orderRelevant = orderRelevant;
	}
	
	public void setTypeValueListElement(AasSubmodelElements typeValueListElement) {
		m_typeValueListElement = typeValueListElement;
	}
	
	public void setValueTypeListElement(DataTypeDefXsd valueTypeListElement) {
		m_valueTypeListElement = valueTypeListElement;
	}
	
	public List<E> getElementAll() {
		return m_members;
	}
	
	public void setElementAll(List<E> members) {
		m_members = members;
	}

	@Override
	public SubmodelElementList newSubmodelElement() {
		DefaultSubmodelElementList smc = new DefaultSubmodelElementList.Builder().build();
		updateAasModel(smc);
		return smc;
	}

	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);

		if ( sme instanceof SubmodelElementList sml ) {
			setIdShort(sml.getIdShort());
			setSemanticId(sml.getSemanticId());
			setOrderRelevant(sml.getOrderRelevant());
			setTypeValueListElement(sml.getTypeValueListElement());
			setValueTypeListElement(m_valueTypeListElement);
			
			m_members = FStream.from(sml.getValue())
								.map(member -> {
									E entity = newMemberEntity();
									entity.updateFromAasModel(member);
									return entity;
								})
								.toList();
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementList, but=" + sme);
		}
	}

	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkArgument(sme != null);

		if ( sme instanceof SubmodelElementList sml ) {
			if ( getIdShort() != null ) sml.setIdShort(getIdShort());
			if ( getSemanticId() != null ) sml.setSemanticId(getSemanticId());
			sml.setOrderRelevant(m_orderRelevant);
			if ( m_typeValueListElement != null ) sml.setTypeValueListElement(m_typeValueListElement);
			if ( m_valueTypeListElement != null ) sml.setValueTypeListElement(m_valueTypeListElement);
			
			List<SubmodelElement> elements = FStream.from(m_members)
										.map(entity -> (SubmodelElement)entity.newSubmodelElement())
										.toList();
			sml.setValue(elements);
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementList, but=" + sme);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s(%d)", getClass().getSimpleName(), m_members.size());
	}
}
