package mdt.task;

import java.io.IOException;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import utils.stream.KeyValueFStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.SubmodelUtils.OperationSubmodelDescriptor;
import mdt.model.sm.SubmodelUtils.SubmodelArgumentDescriptor;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 * 연산(Operation) Submodel이 제공하는 AAS {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation}을
 * 호출하는 {@link MDTTask} 구현체.
 * <p>
 * Builder의 {@link Builder#submodelReference(MDTSubmodelReference)}로 지정된 Submodel의
 * {@code Operation} element를 호출한다. 태스크에 기술되지 않은 입력 변수는 Submodel의
 * 인자 기술자에 정의된 기본값으로 채워진다. 그 외의 태스크 수행 절차는
 * {@link AbstractOperationTask}를 따른다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationSubmodelTask extends AbstractOperationTask {
	private static final String IDSHORT_OPERATION = "Operation";

	private final MDTSubmodelReference m_submodelRef;

	private OperationSubmodelTask(Builder builder) {
		super(builder);

		m_submodelRef = builder.m_submodelRef;
	}

	@Override
	protected AASOperationClient newOperationClient(MDTInstanceManager manager) throws IOException {
		m_submodelRef.activate(manager);

		var opRef = DefaultElementReference.newInstance(m_submodelRef, IDSHORT_OPERATION);
		return new AASOperationClient(opRef, m_pollInterval);
	}

	/**
	 * 태스크에 기술된 입력 인자를 연산의 입력 변수에 반영한다.
	 * <p>
	 * 태스크에 기술되지 않은 입력 변수는 연산 Submodel의 인자 기술자에 정의된 기본값이 있는 경우
	 * 그 값으로 채워진다.
	 */
	@Override
	protected void updateWithArguments(MDTInstanceManager manager, AASOperationClient opClient)
		throws IOException, TaskException {
		super.updateWithArguments(manager, opClient);
		
		//
		// 입력 인자가 지정되지 않은 OperationVariable이 있는 경우, Submodel의 인자 기술자에
		// 정의된 기본값으로 채워진다.
		// Submodel에는 입출력(inoutput) 인자 기술자는 없기 때문에, inoutput OpervationVariable은
		// 신경쓸 필요가 없음.
		//
		
		// Submodel의 인자 기술자 로딩한다.
		Submodel opSubmodel = m_submodelRef.get().getSubmodel();
		OperationSubmodelDescriptor opSmDesc = SubmodelUtils.loadOperationSubmodelDescriptor(opSubmodel);
		
		// Missing된 입력 인자기 있는 확인하여 처리
		Map<String,SubmodelArgumentDescriptor> inArgDesc = opSmDesc.getInputs();
		KeyValueFStream.from(inArgDesc)
						// OperationVariable에 대응되는 입력 인자가 있는 경우만 처리
						.match(m_opClient.getInputVariables())
						// 입력 인자가 없는 경우만 처리
						// 만일 입력 인자가 있다면, 그 값이 더 우선순위가 있어 설정할 필요가 없음.
						.filterKey(argId -> !m_inputArguments.containsKey(argId))
						.forEach((argId, match) -> {
							SubmodelArgumentDescriptor argDesc = match._1;
							OperationVariable opv = match._2;
							ElementValue smev = ElementValues.getValue(argDesc.getSubmodelElement());
							ElementValues.update(opv.getValue(), smev);
						});
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder extends AbstractBuilder<Builder,OperationSubmodelTask> {
		private MDTSubmodelReference m_submodelRef = null;

		@Override
		public OperationSubmodelTask build() {
			return new OperationSubmodelTask(this);
		}

		public Builder submodelReference(MDTSubmodelReference ref) {
			m_submodelRef = ref;
			return this;
		}
	}
}
