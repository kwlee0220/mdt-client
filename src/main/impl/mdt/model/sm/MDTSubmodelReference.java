package mdt.model.sm;

import mdt.model.service.MDTInstance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTSubmodelReference extends SubmodelReference, MDTInstanceManagerAwareReference {
	public String getInstanceId();
	public String getSubmodelIdShort();
	
	public String getSubmodelId();
	
	public MDTInstance getInstance();
}
