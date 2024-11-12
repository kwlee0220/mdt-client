package mdt.task;

import mdt.model.sm.SubmodelReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ExecutionTimeUpdater {
	public void setTargetSubmodel(SubmodelReference ref);
}
