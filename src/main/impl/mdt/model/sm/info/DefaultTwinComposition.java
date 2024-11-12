package mdt.model.sm.info;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultTwinComposition extends SubmodelElementCollectionEntity
									implements TwinComposition {
	@PropertyField(idShort="CompositionID") private String compositionID;
	@PropertyField(idShort="CompositionType") private String compositionType;
	
	@SMListField(idShort="ComponentItems", elementClass=DefaultComponentItem.class)
	private List<ComponentItem> componentItems = Lists.newArrayList();
	
	@SMListField(idShort="CompositionDependencies", elementClass=DefaultCompositionDependency.class)
	private List<CompositionDependency> compositionDependencies = Lists.newArrayList();
	
	@Override
	public String getIdShort() {
		return this.compositionID;
	}
}
