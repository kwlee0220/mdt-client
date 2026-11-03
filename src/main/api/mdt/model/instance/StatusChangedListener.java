package mdt.model.instance;

/**
 * MDTInstance의 상태 변화를 통보받는 리스너 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface StatusChangedListener {
	/**
	 * 인스턴스의 상태가 변경되었음을 알리는 콜백 메소드.
	 * <p>
	 * 리스너가 등록된 인스턴스의 상태가 변경될 때마다 호출되며, 변경된 상태를 파라미터로 전달받는다.
	 *
	 * @param newStatus	변경된 인스턴스의 새로운 상태.
	 */
	public void statusChanged(MDTInstanceStatus newStatus);
}
