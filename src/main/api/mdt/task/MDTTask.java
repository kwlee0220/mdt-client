package mdt.task;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.async.Cancellable;

import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;


/**
 * MDT 태스크를 정의하는 인터페이스.
 * <p>
 * MDT 태스크는 {@link MDTInstanceManager}를 통해 태스크 수행에 필요한 정보를 전달받아서 수행한다.
 * 태스크 수행 중 제한 시간을 초과하거나, 사용자에 의해 중단될 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTask extends Cancellable {
	/** 태스크 수행 제한 시간을 지정하는 옵션 이름 (선택). {@link TaskDescriptor#OPTION_TIMEOUT}과 같다. */
	public static final String OPTION_TIMEOUT = TaskDescriptor.OPTION_TIMEOUT;
	/** 원격 연산의 상태 확인(polling) 주기를 지정하는 옵션 이름 (선택). {@link TaskDescriptor#OPTION_POLL_INTERVAL}과 같다. */
	public static final String OPTION_POLL_INTERVAL = TaskDescriptor.OPTION_POLL_INTERVAL;
	/** 태스크 수행 중 사용할 로그 레벨을 지정하는 옵션 이름. {@link TaskDescriptor#OPTION_LOG_LEVEL}과 같다. */
	public static final String OPTION_LOG_LEVEL = TaskDescriptor.OPTION_LOG_LEVEL;
	
	/**
	 * 태스크 기술자를 반환한다.
	 * <p>
	 * 기술자 없이 코드로 직접 만든 태스크(예: builder로 만든 {@code SetTask}, {@code AbstractOperationTask}
	 * 하위 클래스)는 {@code null}을 돌려줄 수 있다.
	 *
	 * @return	태스크 기술자. 없으면 {@code null}.
	 */
	public TaskDescriptor getTaskDescriptor();
	
	/**
	 * 태스크 작업을 수행한다.
	 * <p>
	 * 태스크 수행에 필요한 정보는 입력 인자, 출력 인자, 그리고 옵션을 통해 전달받는다.
	 * 호출한 스레드에서 태스크가 끝날 때까지 대기(blocking)한다.
	 * 제한 시간이 주어진 경우 태스크 수행이 그 시간을 초과하면 수행을 중단하고 {@link TaskException}을
	 * 던져야 한다. 예외의 원인(cause)은 구현에 따라 다르다(예: {@link TimeoutException}).
	 *
	 * @param manager		MDT인스턴스 관리자 객체.
	 * @return	출력 인자 이름을 키로 하는 태스크 수행 결과 맵. 결과가 없으면 빈 맵.
	 * @throws InterruptedException	태스크 수행 중 인터럽트가 발생된 경우.
	 * @throws CancellationException	태스크 수행 중 사용자 요청으로 중단된 경우.
	 * @throws TaskException		태스크 수행 오류 발생으로 실패한 경우.
	 */
	public Map<String,SubmodelElement> run(MDTInstanceManager manager)
		throws InterruptedException, CancellationException, TaskException;
}
