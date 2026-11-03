package mdt.client.operation;


/**
 * HTTP 연산 서버에서 수행되는 연산의 실행 상태.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum OperationStatus {
	/** 연산이 수행 중인 상태. */
	RUNNING,
	/** 연산이 성공적으로 완료된 상태. */
	COMPLETED,
	/** 연산 수행이 실패한 상태. */
	FAILED,
	/** 연산 수행이 취소된 상태. */
	CANCELLED,
}
