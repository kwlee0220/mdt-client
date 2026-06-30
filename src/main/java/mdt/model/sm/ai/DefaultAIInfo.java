package mdt.model.sm.ai;

import mdt.model.DefaultMinResourceRequirements;
import mdt.model.MinResourceRequirements;
import mdt.model.sm.DefaultOperationInfo;
import mdt.model.sm.entity.SMCollectionField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultAIInfo extends DefaultOperationInfo implements AIInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	public DefaultAIInfo() {
		super("AIInfo");
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public MinResourceRequirements getMinResourceRequirements() {
		return minResourceRequirements;
	}
	
	public void setMinResourceRequirements(MinResourceRequirements minResourceRequirements) {
		this.minResourceRequirements = minResourceRequirements;
	}
}