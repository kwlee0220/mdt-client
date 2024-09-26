package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SubmodelElementListEntity<T,E extends T>
												extends AbstractMDTSubmodelElementContainer
												implements SubmodelElementEntity {
	private boolean m_orderRelevant = false;
	private AasSubmodelElements m_typeValueListElement = AasSubmodelElements.SUBMODEL_ELEMENT;
	private DataTypeDefXsd m_valueTypeListElement;
	private List<E> m_elements = Lists.newArrayList();
	
	protected abstract E newElementEntity();
	
	protected SubmodelElementListEntity(String idShort, String idShortRef, boolean orderRelevant,
											AasSubmodelElements typeValueListElement) {
		super(idShort, idShortRef);
		
		m_orderRelevant = orderRelevant;
		m_typeValueListElement = typeValueListElement;
	}
	protected SubmodelElementListEntity() {
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
	
	public List<? extends T> getElements() {
		return m_elements;
	}
	
	public List<E> getElementHandles() {
		return m_elements;
	}
	
	public void setElementHandles(List<E> elements) {
		m_elements = elements;
	}
	
	@Override
	public SubmodelElementList toAasModel() {
		List<SubmodelElement> submodelElements = FStream.from(m_elements)
														.map(this::toSubmodelElement)
														.toList();
		return new DefaultSubmodelElementList.Builder()
						.idShort(getIdShort())
						.orderRelevant(m_orderRelevant)
						.typeValueListElement(m_typeValueListElement)
						.valueTypeListElement(m_valueTypeListElement)
						.value(submodelElements)
						.build();
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		if ( model instanceof SubmodelElementList sml ) {
			m_elements.clear();
			sml.getValue().stream().map(this::loadElementHandle);
			
			setIdShort(sml.getIdShort());
			setCategory(sml.getCategory());
			setOrderRelevant(sml.getOrderRelevant());
			setTypeValueListElement(sml.getTypeValueListElement());
			setValueTypeListElement(sml.getValueTypeListElement());
		}
		else {
			throw new IllegalArgumentException("SubmodelElementList is required: " + model);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s(%d)", getClass().getSimpleName(), m_elements.size());
	}
	
	private SubmodelElement toSubmodelElement(E handle) {
		return (handle instanceof SubmodelElement sme) ? sme : ((SubmodelElementEntity)handle).toAasModel();
	}
	
	@SuppressWarnings("unchecked")
	private E loadElementHandle(SubmodelElement sme) {
		E handle = newElementEntity();
		if ( handle == null ) {
			return (E)sme;
		}
		else if ( handle instanceof SubmodelElementEntity ) {
			((SubmodelElementEntity)handle).fromAasModel(sme);
			return handle;
		}
		else {
			throw new IllegalArgumentException("Invalid ElementHandle: " + handle);
		}
	}
}
