package mdt.model.sm.info;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionDependency extends SubmodelElementCollectionEntity
											implements CompositionDependency {
	@PropertyField(idShort="SourceId") private String sourceId;
	@PropertyField(idShort="TargetId") private String targetId;
	@PropertyField(idShort="DependencyType") private String dependencyType;
	
	@Override
	public String toString() {
		return String.format("%s[%s->%s]", getDependencyType(), getSourceId(), getTargetId());
	}
}
