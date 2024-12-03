package mdt.model.sm.simulation;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
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
	
	public DefaultModelFileVersion() {
		setIdShort("Model");
	}
}