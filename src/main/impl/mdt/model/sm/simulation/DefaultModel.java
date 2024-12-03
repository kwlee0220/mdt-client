package mdt.model.sm.simulation;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
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
	
	public DefaultModel() {
		setIdShort("Model");
	}
}