package mdt.model;

import mdt.aas.SubmodelRegistry;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.MDTWorkflowManager;

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
