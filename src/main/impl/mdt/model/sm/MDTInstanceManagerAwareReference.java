package mdt.model.sm;

import mdt.model.instance.MDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManagerAwareReference {
	public boolean isActivated();
	public void activate(MDTInstanceManager manager);
}
