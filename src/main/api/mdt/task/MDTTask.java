package mdt.task;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import utils.async.Cancellable;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.variable.Variable;
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
//	/**
//	 * MDT 태스크의 식별자를 반환한다.
//	 *
//	 * @return	MDT 태스크 식별자
//	 */
//	public String getId();
	
	/**
	 * 태스크 기술자를 반환한다.
	 *
	 * @return	태스크 기술자.
	 */
	public TaskDescriptor getTaskDescriptor();
	
	/**
	 * 등록된 모든 입력 태스크 포트들의 리스트를 반환한다.
	 *
	 * @return	태스크 variable 리스트.
	 */
	public default List<Variable> getInputVariableAll() {
		return getTaskDescriptor().getInputVariables();
	}
	
	/**
	 * 주어진 이름에 해당하는 입력 태스크 포트를 반환한다.
	 *
	 * @param name	태스크 이름.
	 * @return	{@link Variable} 객체.
	 */
	public default Variable getInputVariable(String name) {
		return getTaskDescriptor().getInputVariables().getOfKey(name);
	}
	
	/**
	 * 새로운 입력 태스크 포트를 등록한다.
	 *
	 * @param var	등록할 태스크 포트를.	
	 */
	public default void addInputVariable(Variable var) {
		getTaskDescriptor().getInputVariables().add(var);
	}
	
	/**
	 * 등록된 모든 출력 태스크 포트들의 리스트를 반환한다.
	 *
	 * @return	태스크 variable 리스트.
	 */
	public default List<Variable> getOutputVariableAll() {
		return getTaskDescriptor().getOutputVariables();
	}
	
	/**
	 * 주어진 이름에 해당하는 출력 태스크 포트를 반환한다.
	 *
	 * @param name	태스크 이름.
	 * @return	{@link Variable} 객체.
	 */
	public default Variable getOutputVariable(String name) {
		return getTaskDescriptor().getOutputVariables().getOfKey(name);
	}
	
	/**
	 * 새로운 출력 태스크 포트를 등록한다.
	 *
	 * @param var	등록할 태스크 포트를.	
	 */
	public default void addOutputVariable(Variable var) {
		getTaskDescriptor().getOutputVariables().add(var);
	}
	
	/**
	 * 태스크 작업을 수행한다.
	 * <p>
	 * 태스크 수행에 필요한 정보는 입력 파라미터, 출력 파라미터, 그리고 옵션을 통해 전달받는다.
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
