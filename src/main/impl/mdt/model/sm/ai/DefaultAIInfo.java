package mdt.model.sm.ai;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultMinResourceRequirements;
import mdt.model.MinResourceRequirements;
import mdt.model.sm.DefaultOperationInfo;
import mdt.model.sm.entity.SMCollectionField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultAIInfo extends DefaultOperationInfo implements AIInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	public DefaultAIInfo() {
		super("AIInfo");
	}
}