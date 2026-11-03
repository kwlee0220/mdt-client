package mdt.model.sm;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.experimental.Delegate;

import utils.KeyValue;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.MDTModelSerDe;
import mdt.model.ReferenceUtils;
import mdt.model.SubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;


/**
 * 연산형 Submodel(Data, AI, Simulation)에 대한 {@link SubmodelService} 래퍼 클래스.
 * <p>
 * 모든 {@link SubmodelService} 메소드는 생성자에 전달된 서비스로 위임된다.
 * 생성 시 Submodel 메타데이터의 semanticId를 읽어 입출력 인자가 저장된 경로의 접두어를
 * 결정한다 ({@link Data#SEMANTIC_ID} → {@code DataInfo}, {@link AI#SEMANTIC_ID} → {@code AIInfo},
 * {@link Simulation#SEMANTIC_ID} → {@code SimulationInfo}, 그 외에는 빈 문자열).
 * 입출력 인자는 {@code <접두어>.Inputs}/{@code <접두어>.Outputs} 경로의
 * SubmodelElementList로 표현되며, {@link ArgumentCollection}을 통해 접근한다.
 * <p>
 * Submodel에 정의된 {@code Operation} 요소는 {@link #run(Map, Duration, Duration)}으로 호출한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationSubmodelService implements SubmodelService {
	/** {@link SubmodelService} 메소드 호출을 위임받는 대상 Submodel 서비스. */
	@Delegate protected final SubmodelService m_service;
	private final Submodel m_metadata;
	private final String m_pathPrefix;
	
	/**
	 * 주어진 Submodel 서비스를 감싸는 객체를 생성한다.
	 * <p>
	 * 생성 과정에서 대상 Submodel의 메타데이터({@link Modifier#METADATA})를 원격으로 읽는다.
	 *
	 * @param service	대상 Submodel 서비스.
	 */
	public OperationSubmodelService(SubmodelService service) {
		m_service = service;
		
		m_metadata = m_service.getSubmodel(Modifier.METADATA);

		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(m_metadata.getSemanticId());
		// semanticId가 없으면 알려진 연산 Submodel이 아니므로 접두어 없이(default와 같이) 다룬다.
		m_pathPrefix = (semanticId == null) ? "" : switch ( semanticId ) {
			case Data.SEMANTIC_ID -> "DataInfo";
			case AI.SEMANTIC_ID -> "AIInfo";
			case Simulation.SEMANTIC_ID -> "SimulationInfo";
			default -> "";
		};
	}
	
	/**
	 * 입력 인자 목록({@code <접두어>.Inputs})을 읽어 {@link ArgumentCollection}으로 반환한다.
	 * <p>
	 * 호출할 때마다 원격 Submodel에서 인자 목록을 새로 읽는다.
	 *
	 * @return	입력 인자 {@link ArgumentCollection}.
	 */
	public ArgumentCollection getInputs() {
		return new ArgumentCollection("Input");
	}
	
	/**
	 * 출력 인자 목록({@code <접두어>.Outputs})을 읽어 {@link ArgumentCollection}으로 반환한다.
	 * <p>
	 * 호출할 때마다 원격 Submodel에서 인자 목록을 새로 읽는다.
	 *
	 * @return	출력 인자 {@link ArgumentCollection}.
	 */
	public ArgumentCollection getOutputs() {
		return new ArgumentCollection("Output");
	}
	
	/**
	 * Submodel의 {@code Operation} 요소를 주어진 입력 값으로 호출하고 종료될 때까지 대기한다.
	 * <p>
	 * 호출은 {@link AASOperationClient}를 통해 수행되며, 연산 종료 여부를 {@code pollInterval}
	 * 간격으로 확인한다. {@code inputVariables} 중 연산에 정의되지 않은 이름의 인자는 무시된다.
	 *
	 * @param inputVariables	입력 인자 이름과 값 SubmodelElement의 맵.
	 * @param timeout	최대 대기 시간. {@code null}이면 제한 없음.
	 * @param pollInterval	연산 종료 여부 확인 간격.
	 * @return	출력 인자의 idShort와 그 값({@link ElementValue})의 맵.
	 * @throws CancellationException	연산이 취소된 경우.
	 * @throws InterruptedException	대기 중 쓰레드가 인터럽트된 경우.
	 * @throws TimeoutException	제한 시간 내에 연산이 종료되지 않은 경우.
	 * @throws ExecutionException	연산 수행이 실패하거나 연산 정보를 읽는 중 {@link IOException}이
	 * 			발생한 경우.
	 */
	public Map<String,ElementValue> run(Map<String,SubmodelElement> inputVariables, Duration timeout,
										Duration pollInterval)
		throws CancellationException, InterruptedException, TimeoutException, ExecutionException {
		try {
			AASOperationClient opClient = new AASOperationClient(this, "Operation", pollInterval);
			opClient.setTimeout(timeout);
			
			inputVariables.forEach((argId, argElm) -> {
				opClient.setInputVariable(argId, argElm);
			});
			OperationResult result = opClient.run();
			return FStream.from(result.getOutputArguments())
					.mapToKeyValue(opVar -> {
						String argId = opVar.getValue().getIdShort();
						ElementValue argValue = ElementValues.getValue(opVar.getValue());
						
						return KeyValue.of(argId, argValue);
					})
					.toMap();
		}
		catch ( IOException e ) {
			throw new ExecutionException(e);
		}
	}
	
	/**
	 * 연산형 Submodel에 정의된 입력 또는 출력 인자 목록을 다루는 클래스.
	 * <p>
	 * 인자 목록은 {@code <접두어>.Inputs}(또는 {@code Outputs}) SubmodelElementList이고,
	 * 각 원소는 {@code InputID}/{@code InputValue}(또는 {@code OutputID}/{@code OutputValue})
	 * 필드를 갖는 SubmodelElementCollection이다. 생성 시 원격 Submodel에서 목록을 읽어
	 * 로컬에 보관하며, {@code setValue} 계열 메소드는 로컬 사본만 변경하므로
	 * 원격에 반영하려면 {@link #save()}를 호출해야 한다.
	 */
	public class ArgumentCollection {
		private final String m_argKind;
		private final String m_path;
		private SubmodelElementList m_arguments;
		private Map<String,SubmodelElement> m_argMap;
		
		private ArgumentCollection(String argKind) {
			m_argKind = argKind;
			m_path = String.format("%s.%ss", m_pathPrefix, argKind);
			
			load();
		}
		
		/**
		 * 원격 Submodel에서 인자 목록 SubmodelElementList를 다시 읽어 로컬 사본을 갱신한다.
		 */
		public void load() {
			m_arguments = (SubmodelElementList)m_service.getSubmodelElementByPath(m_path);
			m_argMap = FStream.from(m_arguments.getValue())
								.cast(SubmodelElementCollection.class)
								.mapToKeyValue(smc -> {
									String id = SubmodelUtils.getStringFieldById(smc, String.format("%sID", m_argKind));
									SubmodelElement value = SubmodelUtils.getFieldById(smc, String.format("%sValue", m_argKind));
									return KeyValue.of(id, value);
								})
								.toMap();
		}
		
		/**
		 * 로컬 사본의 인자 목록 SubmodelElementList로 원격 Submodel의 해당 요소를 갱신한다.
		 */
		public void save() {
			m_service.updateSubmodelElementByPath(m_path, m_arguments);
		}
		
		/**
		 * 인자 식별자({@code ...ID} 필드 값)와 인자 값({@code ...Value} 필드의 값)의 맵을 반환한다.
		 *
		 * @return	인자 식별자와 값({@link ElementValue})의 맵.
		 */
		public Map<String,ElementValue> getValue() {
			ElementListValue elv = (ElementListValue)ElementValues.getValue(m_arguments);
			return FStream.from(elv.getElementValues())
							.cast(ElementCollectionValue.class)
							.mapToKeyValue(ecv -> {
								StringPropertyValue id = (StringPropertyValue)ecv.getField(String.format("%sID", m_argKind));
								ElementValue value = ecv.getField(String.format("%sValue", m_argKind));
								return KeyValue.of(""+id.toValueObject(), value);
							})
							.toMap();
		}
		
		/**
		 * 주어진 맵에 포함된 인자들의 값을 로컬 사본에 반영한다.
		 * <p>
		 * 맵에 없는 인자는 변경되지 않는다. 원격에 반영하려면 {@link #save()}를 호출해야 한다.
		 *
		 * @param values	인자 식별자와 새 값의 맵.
		 * @return	이 객체.
		 */
		public ArgumentCollection setValue(Map<String,ElementValue> values) {
			for ( SubmodelElement sme: m_arguments.getValue() ) {
				if ( sme instanceof SubmodelElementCollection smc ) {
					String id = SubmodelUtils.getStringFieldById(smc, String.format("%sID", m_argKind));
					ElementValue ev = values.get(id);
					if ( ev != null ) {
						SubmodelElement value = SubmodelUtils.getFieldById(sme, String.format("%sValue", m_argKind));
						ElementValues.update(value, ev);
					}
				}
			}
			return this;
		}
		/**
		 * 주어진 식별자의 인자 값을 로컬 사본에 반영한다.
		 * <p>
		 * 해당 식별자의 인자가 없으면 아무 것도 하지 않는다.
		 * 원격에 반영하려면 {@link #save()}를 호출해야 한다.
		 *
		 * @param id	인자 식별자.
		 * @param value	새 인자 값.
		 * @return	이 객체.
		 */
		public ArgumentCollection setValue(String id, ElementValue value) {
			SubmodelElement arg = m_argMap.get(id);
			if ( arg != null ) {
				ElementValues.update(arg, value);
			}
			return this;
		}
		
		/**
		 * 각 인자의 값 SubmodelElement를 복사하여 idShort를 인자 식별자로 바꾼
		 * {@link OperationVariable} 목록으로 변환한다.
		 *
		 * @return	{@link OperationVariable} 목록.
		 */
		public List<OperationVariable> toOperationVariables() {
			return KeyValueFStream.from(m_argMap)
							.mapOrThrow((argId, argValue) -> {
								JsonNode jnode = MDTModelSerDe.toJsonNode(argValue);
								SubmodelElement copied = MDTModelSerDe.readValue(jnode, SubmodelElement.class);
								copied.setIdShort(argId);
								
								return (OperationVariable)new DefaultOperationVariable.Builder()
																	.value(copied)
																	.build();
							})
							.toList();
		}
	}
}
