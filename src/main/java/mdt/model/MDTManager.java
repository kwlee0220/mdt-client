package mdt.model;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.workflow.MDTWorkflowManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTManager {
	public <T extends MDTService> T getService(Class<T> svcClass);
	
	public MDTInstanceManager getInstanceManager();
	public MDTWorkflowManager getWorkflowManager();
	public SubmodelRegistry getSubmodelRegistry();
}
