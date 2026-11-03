package mdt.model.sm.simulation;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link ModelFileVersion}의 기본 구현 엔티티.
 * <p>
 * 각 필드는 같은 이름의 Property에 대응된다. 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultModelFileVersion extends SubmodelElementCollectionEntity implements ModelFileVersion {
	@PropertyField(idShort="ModelVersionId") private String modelVersionId;
	@PropertyField(idShort="ModelPreviewImage") private String modelPreviewImage;
	@PropertyField(idShort="DigitalFile") private String digitalFile;
	@PropertyField(idShort="ModelFileReleaseNotesTxt") private String modelFileReleaseNotesTxt;
	@PropertyField(idShort="ModelFileReleaseNotesFile") private String modelFileReleaseNotesFile;
	
	/**
	 * 빈 모델 파일 버전 정보 엔티티를 생성한다. idShort는 {@code ModelFileVersion}으로 초기화된다.
	 */
	public DefaultModelFileVersion() {
		setIdShort("ModelFileVersion");
	}
}