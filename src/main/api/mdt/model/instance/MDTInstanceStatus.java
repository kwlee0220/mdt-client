package mdt.model.instance;


/**
 * {@link MDTInstance}의 생명주기 상태를 표현하는 열거형.
 * <p>
 * MDT 인스턴스는 {@link MDTInstanceManager}에 등록된 시점부터 제거되기까지 다음과 같은
 * 상태들을 거치며, 각 상태는 인스턴스의 운영 가능 여부와 진행 중인 작업을 나타낸다.
 * <p>
 * 일반적인 상태 전이는 다음과 같다:
 * <pre>
 *   (등록 요청)  → {@link #ADDING}   → {@link #STOPPED} | {@link #REMOVED}
 *   {@link #STOPPED}     → {@link #STARTING} → {@link #RUNNING} | {@link #FAILED}
 *   {@link #RUNNING}     → {@link #STOPPING} → {@link #STOPPED} | {@link #FAILED}
 *   {@link #STOPPED} | {@link #FAILED}  → (제거)   → {@link #REMOVED}
 * </pre>
 * 이 중 {@link #ADDING}, {@link #STARTING}, {@link #STOPPING}은 일시적인 전이 상태이며,
 * {@link #STOPPED}, {@link #RUNNING}, {@link #FAILED}, {@link #REMOVED}는 안정 상태이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum MDTInstanceStatus {
	/**
	 * 인스턴스가 {@link MDTInstanceManager}에 등록되는 중인 상태.
	 * <p>
	 * 등록 작업이 성공적으로 완료되면 {@link #STOPPED} 상태로 전이되고,
	 * 등록에 실패하면 {@link #REMOVED} 상태로 전이된다.
	 */
	ADDING,

	/**
	 * 인스턴스가 등록되어 있지만 실행 중이 아닌 상태.
	 * <p>
	 * 이 상태에서는 인스턴스의 서비스 endpoint를 사용할 수 없으며,
	 * 인스턴스를 시작하거나 제거할 수 있다.
	 */
	STOPPED,

	/**
	 * 인스턴스가 시작되는 중인 상태.
	 * <p>
	 * 시작 작업이 성공적으로 완료되면 {@link #RUNNING} 상태로 전이되고,
	 * 시작에 실패하면 {@link #FAILED} 상태로 전이된다.
	 */
	STARTING,

	/**
	 * 인스턴스가 정상적으로 실행 중인 상태.
	 * <p>
	 * 이 상태에서만 인스턴스의 서비스 endpoint가 활성화되어 외부 요청을 처리할 수 있다.
	 */
	RUNNING,

	/**
	 * 인스턴스가 정지되는 중인 상태.
	 * <p>
	 * 정지 작업이 성공적으로 완료되면 {@link #STOPPED} 상태로 전이된다.
	 */
	STOPPING,

	/**
	 * 인스턴스의 시작 또는 운영 중 오류가 발생한 상태.
	 * <p>
	 * {@link #STOPPED}와 마찬가지로 이 상태에서도 인스턴스를 다시 시작하거나 제거할 수 있다.
	 */
	FAILED,

	/**
	 * 인스턴스가 {@link MDTInstanceManager}에서 제거된 상태.
	 * <p>
	 * 정상적으로 제거된 경우와 등록 단계에서 실패하여 제거된 경우 모두 이 상태로 표현되며,
	 * 더 이상 어떤 상태로도 전이되지 않는 종결 상태이다.
	 */
	REMOVED,
}
