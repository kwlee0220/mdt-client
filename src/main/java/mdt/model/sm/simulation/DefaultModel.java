package mdt.model.sm.simulation;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * 시뮬레이션 {@link Model}의 기본 구현 엔티티 ({@code Model} SubmodelElementCollection).
 * <p>
 * {@code ModelFileType}, {@code LastExecutionTime} Property와 {@code ModelFileVersion}
 * SubmodelElementCollection을 필드로 갖는다. 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultModel extends SubmodelElementCollectionEntity implements Model {
	@PropertyField(idShort="ModelFileType")
	private String modelFileType;
	
	@SMCollectionField(idShort="ModelFileVersion", adaptorClass=DefaultModelFileVersion.class)
	private ModelFileVersion modelFileVersion;
	
	@PropertyField(idShort="LastExecutionTime", valueType="Duration")
	private Duration lastExecutionTime;
	
	/**
	 * idShort가 {@code Model}인 빈 시뮬레이션 모델 정보 엔티티를 생성한다.
	 */
	public DefaultModel() {
		setIdShort("Model");
	}
}