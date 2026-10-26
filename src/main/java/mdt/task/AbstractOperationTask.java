package mdt.task;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.KeyValue;
import utils.LoggerSettable;
import utils.Throwables;
import utils.func.FOption;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.task.TaskArgument.LiteralArgument;
import mdt.task.TaskArgument.ReferenceArgument;
import mdt.workflow.model.TaskDescriptor;


/**
 * AAS {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation} 호출 기반 {@link MDTTask} 구현체들의
 * 공통 기반 클래스.
 * <p>
 * 대상 연산의 {@link AASOperationClient}를 통해 연산을 비동기로 호출하고, 완료될 때까지
 * 주기적으로 실행 상태를 확인한다. 태스크 수행은 다음 순서로 진행된다:
 * <ol>
 * 	<li>{@link #newOperationClient(MDTInstanceManager)}로 대상 연산의 {@link AASOperationClient}를 생성한다.</li>
 * 	<li>{@link #updateWithArguments(MDTInstanceManager, AASOperationClient)}로 태스크에 기술된 인자를
 * 		연산의 입력 및 입출력(inoutput) 변수에 반영한다.</li>
 * 	<li>연산을 호출하고 완료될 때까지 대기한다.</li>
 * 	<li>연산 결과의 출력(output) 및 입출력(inoutput) 값을 동일 이름의 출력/입출력 인자가 가리키는
 * 		MDT 요소에 기록한다.</li>
 * </ol>
 * 대상 연산을 얻는 방법은 하위 클래스가 {@link #newOperationClient(MDTInstanceManager)}를 구현하여
 * 결정하고, 인자 반영 방식을 바꾸려는 하위 클래스는
 * {@link #updateWithArguments(MDTInstanceManager, AASOperationClient)}를 재정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractOperationTask implements MDTTask, LoggerSettable {
	protected static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	protected final Map<String,TaskArgument> m_inputArguments;
	protected final Map<String,TaskArgument.ReferenceArgument> m_inoutputArguments;
	protected final Map<String,TaskArgument.ReferenceArgument> m_outputArguments;
	protected final Duration m_pollInterval;
	protected final Duration m_timeout;

	protected AASOperationClient m_opClient = null;
	private Logger m_logger = null;

	/**
	 * 호출 대상 연산의 {@link AASOperationClient}를 생성한다.
	 *
	 * @param manager	MDT 인스턴스 관리자 객체.
	 * @return	대상 연산의 클라이언트 객체.
	 * @throws IOException		대상 연산을 접근하는 과정에서 입출력 오류가 발생한 경우.
	 * @throws TaskException	대상 연산을 결정할 수 없는 경우.
	 */
	protected abstract AASOperationClient newOperationClient(MDTInstanceManager manager)
		throws IOException, TaskException;

	protected AbstractOperationTask(AbstractBuilder<?,?> builder) {
		m_inputArguments = builder.m_inputArguments;
		m_inoutputArguments = builder.m_inoutputArguments;
		m_outputArguments = builder.m_outputArguments;
		m_pollInterval = FOption.getOrElse(builder.m_pollInterval, DEFAULT_POLL_INTERVAL);
		m_timeout = builder.m_timeout;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return null;
	}

	/**
	 * 대상 AAS 연산을 호출하고 완료될 때까지 대기한 후, 연산의 출력(output) 및
	 * 입출력(inoutput) 값을 반환한다.
	 * <p>
	 * 연산이 정상 완료되면 출력 값은 동일 이름의 출력 인자가 가리키는 MDT 요소에,
	 * 입출력 값은 동일 이름의 입출력 인자가 가리키는 MDT 요소에 각각 기록된 후 반환된다.
	 * 인자 이름 매칭은 양쪽에 모두 존재하는 이름에 대해서만 이루어지며,
	 * 한쪽에만 존재하는 인자는 무시된다.
	 *
	 * @param manager	MDT 인스턴스 관리자 객체.
	 * @return	연산의 출력/입출력 변수 이름(idShort)을 키로 하는 결과 값 맵.
	 * @throws InterruptedException		연산 완료를 대기하는 도중 인터럽트가 발생한 경우.
	 * @throws CancellationException	연산이 취소되었거나 제한 시간을 초과한 경우.
	 * @throws TaskException			연산 수행이 실패했거나, 인자를 읽고 쓰는 과정에서 오류가 발생한 경우.
	 */
	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
		throws InterruptedException, CancellationException, TaskException {
		try {
			m_opClient = newOperationClient(manager);

			// 태스크에 기술된 입력 인자를 연산의 입력 변수에 반영한다.
			updateWithArguments(manager, m_opClient);

			getTimeout().ifPresent(m_opClient::setTimeout);
			OperationResult result = m_opClient.run();

			// 출력 변수의 값을 수집된 출력 값으로 갱신한다.
			FStream.from(result.getOutputArguments())
			        .mapToKeyValue(opv -> KeyValue.of(opv.getValue().getIdShort(), opv.getValue()))
					.match(m_outputArguments)
					.forEach((argId, match) -> {
						SubmodelElement outVal = match._1;
						TaskArgument.ReferenceArgument refArg = match._2;
						try {
							refArg.activate(manager);
							refArg.updateValue(ElementValues.getValue(outVal));
						}
						catch ( IOException e ) {
							getLogger().warn("Failed to update output argument: id={}, cause={}", argId, e);
						}
					});

			// 입출력 변수의 값을 수집된 출력 값으로 갱신한다.
			FStream.from(result.getInoutputArguments())
			        .mapToKeyValue(opv -> KeyValue.of(opv.getValue().getIdShort(), opv.getValue()))
					.match(m_inoutputArguments)
					.forEach((argId, match) -> {
						SubmodelElement outVal = match._1;
						TaskArgument.ReferenceArgument refArg = match._2;
						try {
							refArg.activate(manager);
							refArg.updateValue(ElementValues.getValue(outVal));
						}
						catch ( IOException e ) {
							getLogger().warn("Failed to update inoutput argument: id={}, cause={}", argId, e);
						}
					});

			return FStream.from(result.getOutputArguments())
							.concatWith(result.getInoutputArguments())
					        .mapToKeyValue(opv -> {
					        	SubmodelElement outVal = opv.getValue();
					        	return KeyValue.of(outVal.getIdShort(), outVal);
					        })
							.toMap();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			Throwables.throwIfInstanceOf(cause, InterruptedException.class);
			Throwables.throwIfInstanceOf(cause, CancellationException.class);

			throw new TaskException("fails to execute " + getClass().getSimpleName(), cause);
		}
		catch ( IOException e ) {
			throw new TaskException("fails to execute " + getClass().getSimpleName(), e);
		}
	}

	/**
	 * 수행 중인 연산의 취소를 시도한다.
	 * <p>
	 * 현재 취소를 지원하지 않기 때문에 항상 {@code false}를 반환한다.
	 *
	 * @return	항상 {@code false}.
	 */
	@Override
	public boolean cancel() {
		return false;
	}

	/**
	 * 연산의 실행 상태를 확인하는 주기를 반환한다.
	 *
	 * @return	실행 상태 확인 주기. Builder에서 지정하지 않은 경우는 기본값(3초).
	 */
	public FOption<Duration> getPollInterval() {
		return FOption.ofNullable(m_pollInterval);
	}

	/**
	 * 연산 수행의 전체 제한 시간을 반환한다.
	 *
	 * @return	제한 시간. Builder에서 지정하지 않은 경우는 {@link FOption#empty()}.
	 */
	public FOption<Duration> getTimeout() {
		return FOption.ofNullable(m_timeout);
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, () -> LoggerFactory.getLogger(getClass()));
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}

	/**
	 * 태스크에 기술된 인자를 연산의 입력 및 입출력(inoutput) 변수에 반영한다.
	 * <p>
	 * 기본 구현은 입력 인자를 이름(idShort)이 일치하는 연산의 입력 변수에, 입출력 인자를
	 * 이름이 일치하는 연산의 입출력 변수에 각각 반영한다.
	 * 양쪽에 모두 존재하는 이름에 대해서만 반영되며, 한쪽에만 존재하는 인자는 무시된다.
	 *
	 * @param manager	MDT 인스턴스 관리자 객체.
	 * @param opClient	대상 연산의 클라이언트.
	 * @throws IOException		연산의 입력/입출력 변수를 접근하는 과정에서 입출력 오류가 발생한 경우.
	 * @throws TaskException	인자의 값을 읽는 과정에서 오류가 발생한 경우.
	 */
	protected void updateWithArguments(MDTInstanceManager manager, AASOperationClient opClient)
		throws IOException, TaskException {
		KeyValueFStream.from(m_inputArguments)
						.match(opClient.getInputVariables())
						.forEachOrThrow((argId, match) -> {
							TaskArgument argv = match._1;
							OperationVariable opv = match._2;
							try {
								updateOperationVariable(manager, opv, argv);
							}
							catch ( IOException e ) {
								throw new TaskException("fails to read input argument: id=" + argId, e);
							}
						});

		KeyValueFStream.from(m_inoutputArguments)
						.match(opClient.getInoutputVariables())
						.forEachOrThrow((argId, match) -> {
							TaskArgument argv = match._1;
							OperationVariable opv = match._2;
							try {
								updateOperationVariable(manager, opv, argv);
							}
							catch ( IOException e ) {
								throw new TaskException("fails to read inoutput argument: id=" + argId, e);
							}
						});
	}

	/**
	 * 태스크 인자의 값을 연산의 입력 변수에 반영한다.
	 * <p>
	 * 인자의 종류에 따라 다음과 같이 처리한다:
	 * <ul>
	 * 	<li>{@link ReferenceArgument}: 참조가 가리키는 {@link SubmodelElement} 전체를 읽어 입력 변수의 값을 교체한다.</li>
	 * 	<li>{@link LiteralArgument}: 리터럴 값을 입력 변수의 값에 반영한다.</li>
	 * </ul>
	 * 연산 변수의 이름은 그 값인 {@link SubmodelElement}의 idShort로 결정되기 때문에, 반영 과정에서
	 * 원래의 idShort는 보존된다.
	 *
	 * @param manager	MDT 인스턴스 관리자 객체.
	 * @param opv		값을 반영할 연산의 입력 변수.
	 * @param arg		반영할 태스크 인자. ({@code null} 불가)
	 * @throws IOException				인자의 값을 읽는 과정에서 입출력 오류가 발생한 경우.
	 * @throws IllegalArgumentException	지원하지 않는 종류의 인자가 주어진 경우.
	 */
	protected final void updateOperationVariable(MDTInstanceManager manager, OperationVariable opv,
													TaskArgument arg) throws IOException {
		SubmodelElement opvSme = opv.getValue();

		if ( arg instanceof TaskArgument.ReferenceArgument refArg ) {
			// 'Value' 값만 읽어오는 경우 (특히 SMC/SML 의 경우), 읽어온 값을 모두 반영할 수 없기
			// 때문에 SubmodelElement 전체를 읽어온다.
			// 특히 Timeseries SMC의 경우에는 가변 길이의 값을 지원할 수 없게 됨.
			// 다만, OperationVariable의 SubmodelElement의 idShort가 인자의 이름을 결정하기 때문에
			// 갱신된 SubmodelElement의 idshort은 변경시키지 않는다.
			refArg.activate(manager);
			SubmodelElement newSme = refArg.read();
			newSme.setIdShort(opvSme.getIdShort());
			opv.setValue(newSme);
		}
		else if ( arg instanceof TaskArgument.LiteralArgument literal ) {
			ElementValue argv = literal.readValue();
			
//			if ( !(opvSme instanceof StringPropertyValue) ) {
//				ElementValue v = literal.readValue();
//				if ( v instanceof StringPropertyValue strPv ) {
//					String jsonStr = strPv.toValueJsonString();
//					jsonStr = MDTModelSerDe.MAPPER.readTree(jsonStr).asText();
//					ElementValues.updateWithValueJsonString(opvSme, jsonStr);
//				}
//				else {
//					throw new IllegalArgumentException("Unsupported TaskArgument type: " + arg.getClass().getName());
//				}
//			}
//			else {
				ElementValues.update(opvSme, argv);
//			}
		}
		else {
			// 'TaskOutput'은 읽어오는 것을 지원하지 않음.
			String msg = String.format("Unsupported TaskArgument[%s]: %s",
										opvSme.getIdShort(), arg.getClass().getName());
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * {@link AbstractOperationTask}와 그 하위 클래스들의 builder를 위한 공통 추상 builder 클래스.
	 *
	 * @param <B>	builder 타입 (method chaining을 위한 self type)
	 * @param <T>	build 대상 {@link AbstractOperationTask} 타입
	 */
	public abstract static class AbstractBuilder<B extends AbstractBuilder<B,T>, T extends AbstractOperationTask> {
		protected Map<String,TaskArgument> m_inputArguments = new HashMap<>();
		protected Map<String,TaskArgument.ReferenceArgument> m_inoutputArguments = new HashMap<>();
		protected Map<String,TaskArgument.ReferenceArgument> m_outputArguments = new HashMap<>();
		protected Duration m_pollInterval = null;
		protected Duration m_timeout = null;

		public abstract T build();

		@SuppressWarnings("unchecked")
		protected B self() {
			return (B)this;
		}

		public B inputArguments(Map<String,TaskArgument> args) {
			m_inputArguments.clear();
			m_inputArguments.putAll(args);
			return self();
		}

		public B addInputArgument(String id, TaskArgument arg) {
			m_inputArguments.put(id, arg);
			return self();
		}
		
		public B inoutputArguments(Map<String,TaskArgument.ReferenceArgument> args) {
			m_inoutputArguments.clear();
			m_inoutputArguments.putAll(args);
			return self();
		}
		
		public B addInoutputArgument(String id, TaskArgument.ReferenceArgument arg) {
			m_inoutputArguments.put(id, arg);
			return self();
		}

		public B outputArguments(Map<String,TaskArgument.ReferenceArgument> args) {
			m_outputArguments.clear();
			m_outputArguments.putAll(args);
			return self();
		}

		public B addOutputArgument(String id, TaskArgument.ReferenceArgument arg) {
			m_outputArguments.put(id, arg);
			return self();
		}

		public B pollInterval(Duration interval) {
			m_pollInterval = interval;
			return self();
		}

		public B timeout(Duration timeout) {
			m_timeout = timeout;
			return self();
		}
	}
}
