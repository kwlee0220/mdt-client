package mdt.workflow;

/**
 * 워크플로우 및 워크플로우를 구성하는 task의 실행 상태.
 * <p>
 * 각 상태는 한 글자 약칭({@link #getShortName()})을 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum WorkflowStatus {
	/** 아직 시작되지 않은 상태 (약칭 {@code N}). */
	NOT_STARTED("N"),
	/** 시작 요청 후 실행 대기 중인 상태 (약칭 {@code S}). */
	STARTING("S"),
	/** 실행 중인 상태 (약칭 {@code R}). */
	RUNNING("R"),
	/** 성공적으로 완료된 상태 (약칭 {@code C}). */
	COMPLETED("C"),
	/** 실패로 종료된 상태 (약칭 {@code F}). */
	FAILED("F"),
	/** 상태를 판단할 수 없는 경우 (약칭 {@code ?}). */
	UNKNOWN("?");
	
	private String m_shortName;
	
	WorkflowStatus(String shortName) {
		m_shortName = shortName;
	}
	
	/**
	 * 상태의 한 글자 약칭을 반환한다.
	 *
	 * @return	상태 약칭.
	 */
	public String getShortName() {
		return m_shortName;
	}
}
