package mdt.model.sm.ai;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultModel extends SubmodelElementCollectionEntity implements Model {
	@PropertyField(idShort="LearningMethod")
	private String learningMethod;
	
	@PropertyField(idShort="AIModelType")
	private String AIModelType;
	
	@PropertyField(idShort="AIFramework")
	private String AIFramework;
	
	@PropertyField(idShort="ModelDescription")
	private String modelDescription;
	
	@PropertyField(idShort="LastExecutionTime", valueType="Duration")
	private Duration lastExecutionTime;
	
	public DefaultModel() {
		setIdShort("Model");
	}
}