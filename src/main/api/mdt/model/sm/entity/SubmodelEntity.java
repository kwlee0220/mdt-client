package mdt.model.sm.entity;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.Preconditions;
import utils.stream.FStream;


/**
 * AAS {@code Submodel}에 대응되는 엔티티의 기본 클래스.
 * <p>
 * 하위 클래스는 annotation({@link PropertyField}, {@link SMCollectionField} 등)이 붙은 필드를 선언하며,
 * 각 필드는 Submodel의 최상위 SubmodelElement 중 idShort가 일치하는 것에 대응된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelEntity extends AbstractSMEContainerEntity<Submodel> {
	private String m_id;
	
	/**
	 * Submodel의 식별자(id)를 반환한다.
	 *
	 * @return	Submodel 식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * Submodel의 식별자(id)를 설정한다.
	 *
	 * @param id	Submodel 식별자.
	 */
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * 이 엔티티의 id, idShort, semanticId, annotation 필드들로 새 Submodel을 생성한다.
	 *
	 * @return	생성된 Submodel.
	 */
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

	/**
	 * 주어진 Submodel의 idShort, id, semanticId, SubmodelElement들로 이 엔티티를 갱신한다.
	 *
	 * @param model	읽어 들일 Submodel.
	 * @throws IllegalArgumentException	{@code model}이 {@code null}인 경우.
	 * @throws mdt.model.ModelGenerationException	SubmodelElement를 필드 값으로 변환하지 못한 경우.
	 */
	@Override
	public void updateFromAasModel(Submodel model) {
		Preconditions.checkNotNullArgument(model, "model is null");
		
		setIdShort(model.getIdShort());
		setId(model.getId());
		setSemanticId(model.getSemanticId());
		
		updateFields(model.getSubmodelElements());
	}

	/**
	 * 이 엔티티의 내용을 주어진 Submodel에 반영한다.
	 * <p>
	 * id, idShort, semanticId는 {@code null}이 아닌 경우에만 설정한다. 필드로부터 생성된 SubmodelElement는
	 * 같은 idShort의 기존 요소를 대체하고, 대응되는 필드가 없는 기존 요소는 유지된다.
	 *
	 * @param model	갱신할 Submodel.
	 */
	@Override
	public void updateAasModel(Submodel model) {
		if ( getId() != null ) model.setId(getId());
		if ( getIdShort() != null ) model.setIdShort(getIdShort());
		if ( getSemanticId() != null ) model.setSemanticId(getSemanticId());
		
		Map<String, SubmodelElement> elementMap = FStream.from(model.getSubmodelElements())
														.tagKey(SubmodelElement::getIdShort)
														.toMap(Maps.newLinkedHashMap());	// 기존 요소 순서를 유지한다.
		for ( SubmodelElement newElm: super.readSubmodelElementFromFields() ) {
			elementMap.put(newElm.getIdShort(), newElm);
		}
		model.setSubmodelElements(Lists.newArrayList(elementMap.values()));
	}
}
