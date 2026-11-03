package mdt.model.sm.entity;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.google.common.collect.Lists;

import utils.Preconditions;
import utils.stream.FStream;


/**
 * AAS {@code SubmodelElementList}에 대응되는 엔티티의 기본 클래스.
 * <p>
 * 리스트의 각 원소는 {@link #newMemberEntity()}가 생성하는 원소 엔티티로 표현된다.
 *
 * @param <E>	원소 엔티티의 타입.
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
	
	/**
	 * SubmodelElementList의 원소 하나에 대응될 빈 원소 엔티티를 생성한다.
	 *
	 * @return	새 원소 엔티티.
	 */
	protected abstract E newMemberEntity();
	
	/**
	 * SubmodelElementList의 {@code orderRelevant} 값을 설정한다.
	 *
	 * @param orderRelevant	원소 순서가 의미를 가지면 {@code true}.
	 */
	public void setOrderRelevant(boolean orderRelevant) {
		m_orderRelevant = orderRelevant;
	}
	
	/**
	 * SubmodelElementList의 {@code typeValueListElement} 값을 설정한다.
	 *
	 * @param typeValueListElement	원소 SubmodelElement 종류.
	 */
	public void setTypeValueListElement(AasSubmodelElements typeValueListElement) {
		m_typeValueListElement = typeValueListElement;
	}
	
	/**
	 * SubmodelElementList의 {@code valueTypeListElement} 값을 설정한다.
	 *
	 * @param valueTypeListElement	원소 Property의 value 타입.
	 */
	public void setValueTypeListElement(DataTypeDefXsd valueTypeListElement) {
		m_valueTypeListElement = valueTypeListElement;
	}
	
	/**
	 * 원소 엔티티 리스트를 반환한다.
	 *
	 * @return	원소 엔티티 리스트.
	 */
	public List<E> getElementAll() {
		return m_members;
	}
	
	/**
	 * 원소 엔티티 리스트를 설정한다.
	 *
	 * @param members	원소 엔티티 리스트.
	 */
	public void setElementAll(List<E> members) {
		m_members = members;
	}

	/**
	 * 이 엔티티의 내용으로 새 SubmodelElementList를 생성한다.
	 *
	 * @return	생성된 SubmodelElementList.
	 */
	@Override
	public SubmodelElementList newSubmodelElement() {
		DefaultSubmodelElementList smc = new DefaultSubmodelElementList.Builder().build();
		updateAasModel(smc);
		return smc;
	}

	/**
	 * 주어진 SubmodelElementList로 이 엔티티를 갱신한다.
	 * <p>
	 * idShort, semanticId, {@code orderRelevant}, {@code typeValueListElement}, {@code valueTypeListElement}를
	 * 복사하고, 각 원소에 대해 {@link #newMemberEntity()}로 생성한 엔티티를 갱신하여 원소 엔티티 리스트를 대체한다.
	 *
	 * @param sme	읽어 들일 SubmodelElementList.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 SubmodelElementList가 아닌 경우.
	 */
	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");

		if ( sme instanceof SubmodelElementList sml ) {
			setIdShort(sml.getIdShort());
			setSemanticId(sml.getSemanticId());
			setOrderRelevant(sml.getOrderRelevant());
			setTypeValueListElement(sml.getTypeValueListElement());
			setValueTypeListElement(sml.getValueTypeListElement());
			
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

	/**
	 * 이 엔티티의 내용을 주어진 SubmodelElementList에 반영한다.
	 * <p>
	 * idShort, semanticId, {@code typeValueListElement}, {@code valueTypeListElement}는 {@code null}이
	 * 아닌 경우에만 설정하며, 원소 리스트는 각 원소 엔티티의 {@link SubmodelElementEntity#newSubmodelElement()}
	 * 결과로 대체된다.
	 *
	 * @param sme	갱신할 SubmodelElementList.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 SubmodelElementList가 아닌 경우.
	 */
	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");

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
