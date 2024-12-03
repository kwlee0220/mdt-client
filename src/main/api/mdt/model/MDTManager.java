package mdt.model;

import mdt.aas.SubmodelRegistry;
import mdt.model.instance.MDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTManager {
	public <T> T getService(Class<T> svcClass);
	
	public MDTInstanceManager getInstanceManager();
	public SubmodelRegistry getSubmodelRegistry();
}
