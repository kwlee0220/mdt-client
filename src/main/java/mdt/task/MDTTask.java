package mdt.task;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import utils.async.Cancellable;

import mdt.model.instance.MDTInstanceManager;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTask extends Cancellable {
	/**
	 * Task 수행시간을 저장할 Port의 이름.
	 * <p>
	 * Task의 output port에 주어진 이름의 port가 존재하는 경우에는 task 수행 시간을
	 * Port가 가리키는 SubmodelElement에 저장한다.
	 */
	public static final String ELAPSED_TIME_PORT_NAME = "mdt-elapsed-time";
	
	/**
	 * 태스크 작업을 수행한다.
	 * <p>
	 * 태스크 수행에 필요한 정보는 입력 포토, 입출력 포트, 그리고 옵션을 통해 전달받는다.
	 * 수행 
	 * 제한 시간이 주어진 경우에는 태스크 수행이 그 시간을 초과한 경우에는 수행이 중단되고
	 * {@link TimeoutException}가 발생되는 것을 요구한다.
	 * 
	 * @param manager		MDT인스턴스 관리자 객체.
	 * @param inputPorts	태스크 수행 중 사용할 입력 포트 리스트.
	 * @param outputPorts	태스크 수행 중 사용할 출력 포트 리스트.
	 * @param timeout		제한 시간.
	 * 						제한 시간을 설정하지 않는 경우는 {@code null}을 사용한다.
	 * @throws TimeoutException	태스크 수행 중 제한 시간을 초과하여 작업을 멈춘 경우.
	 * @throws InterruptedException	태스크 수행 중 인터럽트가 발생된 경우.
	 * @throws CancellationException	태스크 수행 중 사용자 요청으로 중단된 경우.
	 * @throws ExecutionException		태스크 수행 오류 발생으로 실패한 경우.
	 */
	public void run(MDTInstanceManager manager,
					Map<String,Port> inputPorts,
					Map<String,Port> outputPorts,
					@Nullable Duration timeout)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException;
}
