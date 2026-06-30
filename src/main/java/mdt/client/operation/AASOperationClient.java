package mdt.client.operation;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import utils.async.AbstractPeriodicPoller;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.AASUtils;
import mdt.model.SubmodelService;
import mdt.model.sm.ref.MDTElementReference;

/**
 * AAS {@link Operation}을 비동기적으로 호출하고 완료될 때까지 주기적으로 실행 상태를 확인하는 클라이언트.
 * <p>
 * 본 클래스는 {@link AbstractPeriodicPoller}의 polling lifecycle을 다음과 같이 사용한다:
 * <ol>
 * 		<li>{@link #initializePoller()}: 입력/입출력 변수를 모아 {@link SubmodelService#invokeOperationAsync}로
 * 			연산을 비동기 호출하고 {@link OperationHandle}을 획득한다.</li>
 * 		<li>{@link #tryPoll()}: 획득한 handle로 연산 실행 상태를 주기적으로 조회한다.
 * 			{@code COMPLETED}이면 결과를 반환하여 polling을 종료하고, {@code FAILED}/{@code CANCELED}/{@code TIMEOUT}이면
 * 			예외를 발생시키며, 그 외 진행 중 상태이면 다음 polling을 계속한다.</li>
 * 		<li>{@link #finalizePoller(OperationResult)}: 정상 완료된 경우 결과의 출력/입출력 변수를 내부 상태에 반영한다.</li>
 * </ol>
 * Polling 주기는 생성자의 {@code pollInterval}로, 전체 시간 제한은 부모 클래스의
 * {@link #setTimeout(Duration)}/{@code setDue}로 설정한다. 시간 제한이 설정되지 않은 경우
 * 연산 호출의 timeout으로 충분히 긴 값({@code P7D})을 사용한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationClient extends AbstractPeriodicPoller<OperationResult> {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationClient.class);
	private static final javax.xml.datatype.Duration LONG_ENOUGH = AASUtils.DATATYPE_FACTORY.newDuration("P7D");
	
	private final SubmodelService m_submodelSvc;
	private final String m_operationPath;
	private Operation m_operationElm;
	private Map<String,OperationVariable> m_inputs = Maps.newHashMap();
	private volatile Map<String,OperationVariable> m_outputs = Maps.newHashMap();
	private volatile Map<String,OperationVariable> m_inoutputs = Maps.newHashMap();
	private OperationHandle m_opHandle;
	
	/**
	 * 주어진 {@link SubmodelService}와 연산 경로로 클라이언트를 생성한다.
	 * <p>
	 * 생성 시점에 {@link #loadOperation()}을 호출하여 대상 {@link Operation}과 그 입력/출력/입출력 변수를 읽어들인다.
	 *
	 * @param svc			연산이 속한 Submodel에 접근하기 위한 {@link SubmodelService}.
	 * @param operationPath	연산 element의 idShort 경로.
	 * @param pollInterval	연산 실행 상태를 확인하는 polling 주기.
	 * @throws IOException	연산 element를 읽어들이는 과정에서 입출력 오류가 발생한 경우.
	 */
	public AASOperationClient(SubmodelService svc, String operationPath, Duration pollInterval) throws IOException {
		super(pollInterval, true);
		
		setLogger(s_logger);
		m_submodelSvc = svc;
		m_operationPath = operationPath;

		loadOperation();
	}
	
	/**
	 * 주어진 연산 element 참조로 클라이언트를 생성한다.
	 * <p>
	 * {@link MDTElementReference}로부터 {@link SubmodelService}와 연산 경로를 추출하며,
	 * 생성 시점에 {@link #loadOperation()}을 호출하여 대상 {@link Operation}을 읽어들인다.
	 *
	 * @param opRef			대상 연산 element에 대한 참조.
	 * @param pollInterval	연산 실행 상태를 확인하는 polling 주기.
	 * @throws IOException	연산 element를 읽어들이는 과정에서 입출력 오류가 발생한 경우.
	 */
	public AASOperationClient(MDTElementReference opRef, Duration pollInterval) throws IOException {
		super(pollInterval, true);
		
		setLogger(s_logger);
		m_submodelSvc = opRef.getSubmodelService();
		m_operationPath = opRef.getIdShortPathString();
		loadOperation();
	}
	
	/**
	 * 대상 {@link Operation} element를 Submodel로부터 다시 읽어들이고,
	 * 입력/출력/입출력 변수 맵을 갱신한다.
	 *
	 * @throws IOException	연산 element를 읽어들이는 과정에서 입출력 오류가 발생한 경우.
	 */
	public void loadOperation() throws IOException {
		m_operationElm = (Operation)m_submodelSvc.getSubmodelElementByPath(m_operationPath);
		
		m_inputs = toMap(m_operationElm.getInputVariables());
		m_outputs = toMap(m_operationElm.getOutputVariables());
		m_inoutputs = toMap(m_operationElm.getInoutputVariables());
	}
	
	/**
	 * 현재 보관 중인 입력/출력/입출력 변수들을 대상 {@link Operation} element에 반영한 뒤,
	 * 해당 element를 Submodel에 저장한다.
	 *
	 * @throws IOException	연산 element를 저장하는 과정에서 입출력 오류가 발생한 경우.
	 */
	public void saveOperation() throws IOException {
		m_operationElm.setInputVariables(FStream.from(m_inputs.values()).toList());
		m_operationElm.setOutputVariables(FStream.from(m_outputs.values()).toList());
		m_operationElm.setInoutputVariables(FStream.from(m_inoutputs.values()).toList());
		
		m_submodelSvc.setSubmodelElementByPath(m_operationPath, m_operationElm);
	}
	
	/**
	 * Polling 시작 시 1회 호출되어 연산을 비동기로 호출하고 {@link OperationHandle}을 획득한다.
	 * <p>
	 * 부모 클래스에 설정된 timeout이 있으면 그 값을, 없으면 {@code LONG_ENOUGH}({@code P7D})를
	 * 연산 호출의 timeout으로 사용한다.
	 */
	@Override
	protected void initializePoller() throws Exception {
		List<OperationVariable> inputs = m_inputs.values().stream().toList();
		List<OperationVariable> inoutputs = m_inoutputs.values().stream().toList();
		javax.xml.datatype.Duration jtimeout = (getTimeout() != null)
											? AASUtils.DATATYPE_FACTORY.newDuration(getTimeout().toMillis())
											: LONG_ENOUGH;
		
		m_opHandle = m_submodelSvc.invokeOperationAsync(m_operationPath, inputs, inoutputs, jtimeout);
		getLogger().info("invoked operation async, handle={}", m_opHandle.getHandleId());
	}

	/**
	 * 연산의 실행 상태를 1회 조회하여 polling 진행 여부를 결정한다.
	 *
	 * @return	연산이 {@code COMPLETED}이면 그 결과를 담은 {@link FOption},
	 * 			그 외 진행 중 상태이면 {@link FOption#empty()}.
	 * @throws ExecutionException		연산이 {@code FAILED} 상태로 종료된 경우.
	 * @throws CancellationException	연산이 {@code CANCELED} 또는 {@code TIMEOUT} 상태로 종료된 경우.
	 */
	@Override
	protected FOption<OperationResult> tryPoll() throws ExecutionException {
		BaseOperationResult result = m_submodelSvc.getOperationAsyncStatus(m_opHandle);
		getLogger().info("polled operation result: {}", result.getExecutionState());
		switch ( result.getExecutionState() ) {
			case COMPLETED:
				return FOption.of(m_submodelSvc.getOperationAsyncResult(m_opHandle));
			case FAILED:
				throw new ExecutionException(new Exception("operation execution failed: " + toMessage(result)));
			case CANCELED:
				throw new CancellationException("operation execution cancelled: " + toMessage(result));
			case TIMEOUT:
				throw new CancellationException("operation execution timed out: " + toMessage(result));
			default:
				return FOption.empty();
		}
	}
	
	/**
	 * Polling 종료 시 호출되어, 연산이 정상 완료된 경우 결과의 출력/입출력 변수를 내부 상태에 반영한다.
	 *
	 * @param result	정상 완료 시 연산 결과, 취소/timeout/예외 등 비정상 종료 시 {@code null}.
	 */
	@Override
	protected void finalizePoller(OperationResult result) {
		if ( result != null ) {
			m_outputs = toMap(result.getOutputArguments()); 
			m_inoutputs = toMap(result.getInoutputArguments());
		}
	}
	
	/**
	 * 입력 변수들을 idShort를 키로 하는 맵으로 반환한다.
	 *
	 * @return	입력 변수 맵.
	 */
	public Map<String, OperationVariable> getInputVariables() {
		return m_inputs;
	}
	/**
	 * 주어진 이름의 입력 변수 값을 설정한다.
	 * <p>
	 * 해당 이름의 입력 변수가 없으면 아무 동작도 하지 않는다.
	 * {@code value}의 idShort는 변수 이름으로 설정된다.
	 *
	 * @param name	입력 변수의 이름(idShort).
	 * @param value	설정할 값.
	 */
	public void setInputVariable(String name, SubmodelElement value) {
		OperationVariable opVar = m_inputs.get(name);
		if ( opVar != null ) {
			value.setIdShort(name);
			opVar.setValue(value);
		}
	}
	
	/**
	 * 출력 변수들을 idShort를 키로 하는 맵으로 반환한다.
	 * <p>
	 * 연산이 정상 완료된 이후에는 연산 결과의 출력 변수들이 반영되어 있다.
	 *
	 * @return	출력 변수 맵.
	 */
	public Map<String, OperationVariable> getOutputVariables() {
		return m_outputs;
	}
	/**
	 * 주어진 이름의 출력 변수 값을 설정한다.
	 * <p>
	 * 해당 이름의 출력 변수가 없으면 아무 동작도 하지 않는다.
	 * {@code value}의 idShort는 변수 이름으로 설정된다.
	 *
	 * @param name	출력 변수의 이름(idShort).
	 * @param value	설정할 값.
	 */
	public void setOutputVariable(String name, SubmodelElement value) {
		OperationVariable opVar = m_outputs.get(name);
		if ( opVar != null ) {
			value.setIdShort(name);
			opVar.setValue(value);
		}
	}
	
	/**
	 * 입출력 변수들을 idShort를 키로 하는 맵으로 반환한다.
	 * <p>
	 * 연산이 정상 완료된 이후에는 연산 결과의 입출력 변수들이 반영되어 있다.
	 *
	 * @return	입출력 변수 맵.
	 */
	public Map<String, OperationVariable> getInoutputVariables() {
		return m_inoutputs;
	}
	/**
	 * 주어진 이름의 입출력 변수 값을 설정한다.
	 * <p>
	 * 해당 이름의 입출력 변수가 없으면 아무 동작도 하지 않는다.
	 * {@code value}의 idShort는 변수 이름으로 설정된다.
	 *
	 * @param name	입출력 변수의 이름(idShort).
	 * @param value	설정할 값.
	 */
	public void setInoutputVariable(String name, SubmodelElement value) {
		OperationVariable opVar = m_inoutputs.get(name);
		if ( opVar != null ) {
			value.setIdShort(name);
			opVar.setValue(value);
		}
	}

	
	/**
	 * {@link OperationVariable} 목록을, 각 변수 값의 idShort를 키로 하는 맵으로 변환한다.
	 *
	 * @param opvIter	변환할 연산 변수 목록.
	 * @return			idShort를 키로 하는 연산 변수 맵.
	 */
	private Map<String, OperationVariable> toMap(Iterable<OperationVariable> opvIter) {
		return FStream.from(opvIter)
						.tagKey(opv -> opv.getValue().getIdShort())
						.toMap();
	}
	
	/**
	 * 연산 결과의 첫 번째 진단 메시지를 {@code "[메시지유형]: 본문"} 형식의 문자열로 만든다.
	 *
	 * @param result	진단 메시지를 담은 연산 결과.
	 * @return			첫 번째 메시지의 문자열 표현. 메시지가 없으면 빈 문자열.
	 */
	private String toMessage(BaseOperationResult result) {
		if ( result.getMessages() == null || result.getMessages().isEmpty() ) {
			return "";
		}
		Message msg = result.getMessages().get(0);
		return String.format("[%s]: %s", msg.getMessageType(), msg.getText());
	}
}
