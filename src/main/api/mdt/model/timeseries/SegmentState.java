package mdt.model.timeseries;


/**
 * 시계열 segment의 상태를 표현하는 열거형.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum SegmentState {
	/** 레코드가 계속 추가되고 있는 진행 중 상태. */
	IN_PROGRESS,
	/** 레코드 추가가 끝나 완료된 상태. */
	COMPLETED,
}
