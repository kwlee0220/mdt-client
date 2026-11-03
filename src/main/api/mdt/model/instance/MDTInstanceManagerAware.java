package mdt.model.instance;

/**
 * {@link MDTInstanceManager}를 주입받아 활성화되는 객체가 구현하는 인터페이스.
 * <p>
 * 생성 시점에는 {@link MDTInstanceManager}를 알 수 없는 객체(예: JSON으로부터 역직렬화된 task 인자)가
 * 이후 {@link #activate(MDTInstanceManager)}를 통해 관리자 객체를 전달받아 사용할 수 있도록 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@FunctionalInterface
public interface MDTInstanceManagerAware {
	/**
	 * 주어진 {@link MDTInstanceManager}를 이용하여 객체를 활성화시킨다.
	 *
	 * @param manager	사용할 {@link MDTInstanceManager} 객체.
	 */
	public void activate(MDTInstanceManager manager);
	
	/**
	 * 주어진 객체가 {@link MDTInstanceManagerAware}를 구현한 경우에만 {@link #activate(MDTInstanceManager)}를
	 * 호출하고, 그 객체를 그대로 반환한다.
	 *
	 * @param <T>		대상 객체의 타입.
	 * @param obj		대상 객체.
	 * @param manager	전달할 {@link MDTInstanceManager} 객체.
	 * @return	인자로 전달된 {@code obj}.
	 */
	public static <T> T activate(T obj, MDTInstanceManager manager) {
		if ( obj instanceof MDTInstanceManagerAware aware ) {
			aware.activate(manager);
		}
		
		return obj;
	}
}
