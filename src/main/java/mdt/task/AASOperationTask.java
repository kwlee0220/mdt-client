package mdt.task;

import java.io.IOException;

import mdt.client.operation.AASOperationClient;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;


/**
 * 참조가 가리키는 AAS {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation}을 호출하는
 * {@link MDTTask} 구현체.
 * <p>
 * Builder의 {@link Builder#operation(MDTElementReference)}로 지정된 참조가 가리키는 연산 element를
 * 호출한다. 태스크 수행 절차는 {@link AbstractOperationTask}를 따른다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask extends AbstractOperationTask {
	private final MDTElementReference m_operationRef;

	private AASOperationTask(Builder builder) {
		super(builder);

		m_operationRef = builder.m_operationRef;
	}

	@Override
	protected AASOperationClient newOperationClient(MDTInstanceManager manager) throws IOException {
		m_operationRef.activate(manager);
		return new AASOperationClient(m_operationRef, m_pollInterval);
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder extends AbstractBuilder<Builder,AASOperationTask> {
		private MDTElementReference m_operationRef = null;

		@Override
		public AASOperationTask build() {
			return new AASOperationTask(this);
		}

		public Builder operation(MDTElementReference ref) {
			m_operationRef = ref;
			return this;
		}
	}
}
