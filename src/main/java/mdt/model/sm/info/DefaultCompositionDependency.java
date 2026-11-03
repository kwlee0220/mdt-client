package mdt.model.sm.info;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link CompositionDependency}의 기본 구현 엔티티.
 * <p>
 * {@code SourceId}, {@code TargetId}, {@code DependencyType} Property를 필드로 갖는다.
 * 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
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
