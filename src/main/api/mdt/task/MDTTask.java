package mdt.task;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import utils.async.Cancellable;

import mdt.model.instance.MDTInstanceManager;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTask extends Cancellable {
	/**
	 * 태스크 작업을 수행한다.
	 * <p>
	 * 태스크 수행에 필요한 정보는 입력 포토, 입출력 포트, 그리고 옵션을 통해 전달받는다.
	 * 수행 
	 * 제한 시간이 주어진 경우에는 태스크 수행이 그 시간을 초과한 경우에는 수행이 중단되고
	 * {@link TimeoutException}가 발생되는 것을 요구한다.
	 * 
	 * @param manager		MDT인스턴스 관리자 객체.
	 * @throws TimeoutException	태스크 수행 중 제한 시간을 초과하여 작업을 멈춘 경우.
	 * @throws InterruptedException	태스크 수행 중 인터럽트가 발생된 경우.
	 * @throws CancellationException	태스크 수행 중 사용자 요청으로 중단된 경우.
	 * @throws TaskException		태스크 수행 오류 발생으로 실패한 경우.
	 */
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException;
}
