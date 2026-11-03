package mdt.model.sm.entity;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.Preconditions;
import utils.stream.FStream;


/**
 * AAS {@code SubmodelElementCollection}에 대응되는 엔티티의 기본 클래스.
 * <p>
 * 하위 클래스는 {@link PropertyField}, {@link SMCollectionField}, {@link SMListField} 등의
 * annotation이 붙은 필드를 선언하며, 각 필드는 annotation에 지정된 idShort의 하위 요소에 대응된다.
 * 필드 값의 읽기/쓰기는 Java Bean getter/setter를 통해 이루어지므로 각 필드에 대한 접근자가 있어야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementCollectionEntity extends AbstractSMEContainerEntity<SubmodelElement>
												implements SubmodelElementEntity {
	/**
	 * 이 엔티티의 idShort, semanticId, annotation이 붙은 필드들로 새 SubmodelElementCollection을 생성한다.
	 *
	 * @return	생성된 SubmodelElementCollection.
	 */
	@Override
	public SubmodelElementCollection newSubmodelElement() {
		DefaultSubmodelElementCollection smc = new DefaultSubmodelElementCollection.Builder()
													.idShort(getIdShort())
													.semanticId(getSemanticId())
													.value(Lists.newArrayList())
													.build();
		updateAasModel(smc);
		return smc;
	}

	/**
	 * 주어진 SubmodelElementCollection의 idShort, semanticId, 하위 요소들로 이 엔티티를 갱신한다.
	 * <p>
	 * 하위 요소는 idShort가 일치하는 annotation 필드에 반영되며, 대응되는 하위 요소가 없는 필드는
	 * 변경되지 않는다.
	 *
	 * @param sme	읽어 들일 SubmodelElementCollection.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 SubmodelElementCollection이 아닌 경우.
	 * @throws mdt.model.ModelGenerationException	하위 요소를 필드 값으로 변환하지 못한 경우.
	 */
	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");
		
		if ( sme instanceof SubmodelElementCollection smc ) {
			setIdShort(smc.getIdShort());
			setSemanticId(smc.getSemanticId());
			
			updateFields(smc.getValue());
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementCollection, but=" + sme);
		}
	}

	/**
	 * 이 엔티티의 내용을 주어진 SubmodelElementCollection에 반영한다.
	 * <p>
	 * idShort와 semanticId는 {@code null}이 아닌 경우에만 설정한다. 필드로부터 생성된 하위 요소는
	 * 같은 idShort의 기존 하위 요소를 대체하고, 대응되는 필드가 없는 기존 하위 요소는 유지된다.
	 *
	 * @param sme	갱신할 SubmodelElementCollection.
	 * @throws IllegalArgumentException	{@code sme}가 {@code null}이거나 SubmodelElementCollection이 아닌 경우.
	 */
	@Override
	public void updateAasModel(SubmodelElement sme) {
		Preconditions.checkNotNullArgument(sme, "sme is null");

		if ( sme instanceof SubmodelElementCollection smc ) {
			if ( getIdShort() != null ) {
				smc.setIdShort(getIdShort());
			}
			if ( getSemanticId() != null ) {
				smc.setSemanticId(getSemanticId());
			}
			
			Map<String, SubmodelElement> elementMap = FStream.from(smc.getValue())
															.tagKey(SubmodelElement::getIdShort)
															.toMap(Maps.newLinkedHashMap());	// 기존 요소 순서를 유지한다.
			for ( SubmodelElement newElm: super.readSubmodelElementFromFields() ) {
				elementMap.put(newElm.getIdShort(), newElm);
			}
			smc.setValue(Lists.newArrayList(elementMap.values()));
		}
		else {
			throw new IllegalArgumentException("Not SubmodelElementCollection, but=" + sme);
		}
	}
}
