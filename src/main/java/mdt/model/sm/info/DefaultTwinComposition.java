package mdt.model.sm.info;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;


import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;

import utils.func.Nullables;

/**
 * {@link TwinComposition}의 기본 구현 엔티티 ({@code TwinComposition} SubmodelElementCollection).
 * <p>
 * {@code CompositionID}, {@code CompositionType} Property와 {@code CompositionItems},
 * {@code CompositionDependencies} SubmodelElementList를 필드로 갖는다.
 * 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultTwinComposition extends SubmodelElementCollectionEntity
									implements TwinComposition {
	@PropertyField(idShort="CompositionID") private String compositionID;
	@PropertyField(idShort="CompositionType") private String compositionType;
	
	@SMListField(idShort="CompositionItems", elementClass=DefaultCompositionItem.class)
	private List<CompositionItem> compositionItems = Lists.newArrayList();
	
	@SMListField(idShort="CompositionDependencies", elementClass=DefaultCompositionDependency.class)
	private List<CompositionDependency> compositionDependencies = Lists.newArrayList();
	
	/**
	 * idShort를 반환한다. idShort가 설정되지 않은 경우는 composition 식별자를 반환한다.
	 *
	 * @return	idShort 또는 composition 식별자.
	 */
	@Override
	public String getIdShort() {
		return Nullables.orElse(super.getIdShort(), this.compositionID);
	}
}
