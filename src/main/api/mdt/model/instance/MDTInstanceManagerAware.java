package mdt.model.instance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@FunctionalInterface
public interface MDTInstanceManagerAware {
	public void activate(MDTInstanceManager manager);
	
	public static <T> T activate(T obj, MDTInstanceManager manager) {
		if ( obj instanceof MDTInstanceManagerAware aware ) {
			aware.activate(manager);
		}
		
		return obj;
	}
}
