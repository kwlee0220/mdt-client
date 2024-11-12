package mdt.model.sm.ai;

import mdt.model.MinResourceRequirements;
import mdt.model.sm.OperationInfo;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AIInfo extends OperationInfo {
//	public Model getModel();
//	public void setModel(Model model);
	
	public MinResourceRequirements getMinResourceRequirements();
	public void setMinResourceRequirements(MinResourceRequirements format);
}
