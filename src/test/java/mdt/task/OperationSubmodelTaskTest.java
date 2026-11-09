package mdt.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import utils.func.FOption;

import mdt.client.operation.AASOperationClient;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;

/**
 * {@link OperationSubmodelTask}가 연산 클라이언트를 만드는 동작을 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationSubmodelTaskTest {
	@Test
	public void newOperationClient_activatesOperationReference() throws Exception {
		SubmodelService service = mock(SubmodelService.class);
		when(service.getSubmodelElementByPath("Operation"))
			.thenReturn(new DefaultOperation.Builder().idShort("Operation").build());
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getSubmodelServiceByIdShort("Sim")).thenReturn(FOption.of(service));
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance("inst")).thenReturn(instance);

		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inst", "Sim");
		OperationSubmodelTask task = OperationSubmodelTask.builder().submodelReference(smRef).build();

		// Submodel 참조만 활성화하고 Operation 요소 참조는 활성화하지 않으면 IllegalStateException이 발생한다.
		AASOperationClient client = task.newOperationClient(manager);

		Assertions.assertNotNull(client);
		verify(service).getSubmodelElementByPath("Operation");
		// updateWithArguments()가 이 참조로 Submodel을 읽는다.
		Assertions.assertTrue(smRef.isActivated());
	}
}
