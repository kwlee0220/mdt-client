package mdt.client.instance;

import mdt.model.instance.MDTInstanceStatus;

/**
 * {@link HttpMDTInstanceClient}의 상태 변경을 통보받기 위한 리스너 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceStatusChangedListener {
	/**
	 * 인스턴스 상태가 변경되었을 때 호출되는 콜백 메소드.
	 *
	 * @param instance	상태가 변경된 인스턴스.
	 * @param status 	변경된 상태.
	 */
	void onStatusChanged(HttpMDTInstanceClient instance, MDTInstanceStatus status);
}
