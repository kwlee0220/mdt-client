package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractMDTSubmodelElementList<E extends MDTSubmodelElement>
												extends AbstractMDTSubmodelElementContainer
												implements MDTSubmodelElement {
	private boolean m_orderRelevant = false;
	private AasSubmodelElements m_typeValueListElement = AasSubmodelElements.SUBMODEL_ELEMENT;
	private DataTypeDefXsd m_valueTypeListElement;
	
	protected abstract E newElement();
	protected abstract List<E> getElements();
	protected abstract void setElements(List<E> elements);
	
	protected AbstractMDTSubmodelElementList(String idShort, String idShortRef, boolean orderRelevant,
												AasSubmodelElements typeValueListElement) {
		super(idShort, idShortRef);
		
		m_orderRelevant = orderRelevant;
		m_typeValueListElement = typeValueListElement;
	}
	protected AbstractMDTSubmodelElementList() {
		this(null, null, false, AasSubmodelElements.SUBMODEL_ELEMENT);
	}
	
	public void setOrderRelevant(boolean orderRelevant) {
		m_orderRelevant = orderRelevant;
	}
	
	public void setTypeValueListElement(AasSubmodelElements typeValueListElement) {
		m_typeValueListElement = typeValueListElement;
	}
	
	public void setValueTypeListElement(DataTypeDefXsd valueTypeListElement) {
		m_valueTypeListElement = valueTypeListElement;
	}
	
	@Override
	public SubmodelElementList toAasModel() {
		List<SubmodelElement> submodelElements = super.fromFields();
		if ( submodelElements.size() == 1 && submodelElements.get(0) instanceof SubmodelElementList sml ) {
			sml.setIdShort(getIdShort());
			sml.setOrderRelevant(m_orderRelevant);
			sml.setTypeValueListElement(m_typeValueListElement);
			sml.setValueTypeListElement(m_valueTypeListElement);
			
			return sml;
		}
		else {
			return new DefaultSubmodelElementList.Builder()
							.idShort(getIdShort())
							.orderRelevant(m_orderRelevant)
							.typeValueListElement(m_typeValueListElement)
							.valueTypeListElement(m_valueTypeListElement)
							.value(submodelElements)
							.build();
		}
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		SubmodelElementList sml = (SubmodelElementList)model;
		List<E> elementAdaptors = FStream.from(sml.getValue())
											.map(elm -> {
												E adaptor = newElement();
												adaptor.fromAasModel(elm);
												return adaptor;
											})
											.toList();
		setElements(elementAdaptors);
	}
	
	@Override
	public String toString() {
		return String.format("%s(%d)", getClass().getSimpleName(), getElements().size());
	}
}
