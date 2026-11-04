package mdt.client.instance;

import org.jetbrains.annotations.Nullable;

import mdt.model.instance.MDTInstanceStatus;


/**
 * 여러 MDTInstance를 대상으로 하는 작업(시작·정지 등)에서 인스턴스 하나의 처리 결과.
 * <p>
 * 작업 클래스는 메시지를 직접 출력하지 않고 이 결과를 돌려주며, 출력과 종료 코드는 호출자(CLI 등)가 정한다.
 *
 * @param instanceId	대상 인스턴스 식별자.
 * @param succeeded		작업 성공 여부.
 * @param finalStatus	작업이 끝난 시점의 인스턴스 상태. 알 수 없으면 {@code null}(예: 인스턴스 조회 실패).
 * @param cause			실패 원인. 성공했으면 {@code null}.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public record InstanceOperationResult(String instanceId, boolean succeeded, @Nullable MDTInstanceStatus finalStatus,
										@Nullable Throwable cause) {
	/**
	 * 성공 결과를 만든다.
	 *
	 * @param instanceId	대상 인스턴스 식별자.
	 * @param finalStatus	작업이 끝난 시점의 상태.
	 * @return	성공 결과.
	 */
	public static InstanceOperationResult success(String instanceId, @Nullable MDTInstanceStatus finalStatus) {
		return new InstanceOperationResult(instanceId, true, finalStatus, null);
	}

	/**
	 * 실패 결과를 만든다.
	 *
	 * @param instanceId	대상 인스턴스 식별자.
	 * @param finalStatus	실패 시점의 상태. 알 수 없으면 {@code null}.
	 * @param cause			실패 원인.
	 * @return	실패 결과.
	 */
	public static InstanceOperationResult failure(String instanceId, @Nullable MDTInstanceStatus finalStatus,
													Throwable cause) {
		return new InstanceOperationResult(instanceId, false, finalStatus, cause);
	}
}
