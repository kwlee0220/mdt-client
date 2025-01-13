package mdt.model.sm.ref;

import mdt.model.instance.MDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManagerAwareReference {
	/**
	 * 본 객체가 활성화되어 있는지 여부를 반환한다.
	 * 
	 * @return 활성화 여부.
	 */
	public boolean isActivated();
	
	/**
	 * 본 객체를 활성화시킨다.
	 *
	 * @param manager	객체 확성화에 사용될 {@link MDTInstanceManager} 객체.
	 */
	public void activate(MDTInstanceManager manager);
}
