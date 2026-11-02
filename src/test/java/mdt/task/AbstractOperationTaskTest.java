package mdt.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mdt.client.operation.AASOperationClient;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;

/**
 * {@link AbstractOperationTask}가 연산 결과를 출력·입출력 인자에 기록하는 동작을 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AbstractOperationTaskTest {
	private static final class TestTask extends AbstractOperationTask {
		private final AASOperationClient m_client;

		private TestTask(Builder builder) {
			super(builder);
			m_client = builder.m_client;
		}

		@Override
		protected AASOperationClient newOperationClient(MDTInstanceManager manager) {
			return m_client;
		}
	}

	private static final class Builder extends AbstractOperationTask.AbstractBuilder<Builder, TestTask> {
		private AASOperationClient m_client;

		@Override
		public TestTask build() {
			return new TestTask(this);
		}
	}

	private MDTInstanceManager m_manager;
	private AASOperationClient m_client;

	@BeforeEach
	public void setUp() throws Exception {
		m_manager = mock(MDTInstanceManager.class);
		m_client = mock(AASOperationClient.class);
		when(m_client.getInputVariables()).thenReturn(Map.of());
		when(m_client.getInoutputVariables()).thenReturn(Map.of());
		OperationResult result = new DefaultOperationResult.Builder()
										.outputArguments(List.of(intVariable("out1", 1), intVariable("out2", 2)))
										.inoutputArguments(List.of(intVariable("io1", 3)))
										.build();
		when(m_client.run()).thenReturn(result);
	}

	private static OperationVariable intVariable(String idShort, int value) {
		return new DefaultOperationVariable.Builder()
					.value(new DefaultProperty.Builder()
								.idShort(idShort)
								.valueType(DataTypeDefXsd.INT)
								.value(Integer.toString(value))
								.build())
					.build();
	}

	private TestTask task(MDTElementReference out1, MDTElementReference out2, MDTElementReference io1) {
		Builder builder = new Builder();
		builder.m_client = m_client;
		return builder.addOutputArgument("out1", TaskArgument.reference(out1))
						.addOutputArgument("out2", TaskArgument.reference(out2))
						.addInoutputArgument("io1", TaskArgument.reference(io1))
						.build();
	}

	@Test
	public void writesAllResults() throws Exception {
		MDTElementReference out1 = mock(MDTElementReference.class);
		MDTElementReference out2 = mock(MDTElementReference.class);
		MDTElementReference io1 = mock(MDTElementReference.class);

		Map<String,SubmodelElement> outputs = task(out1, out2, io1).run(m_manager);

		Assertions.assertEquals(Set.of("out1", "out2", "io1"), outputs.keySet());
		verify(out1).updateValue(any(ElementValue.class));
		verify(out2).updateValue(any(ElementValue.class));
		verify(io1).updateValue(any(ElementValue.class));
	}

	@Test
	public void failsWhenAnyResultCannotBeWritten() throws Exception {
		// 이전에는 기록 실패를 경고 로그로만 남기고 태스크는 성공으로 끝났다.
		MDTElementReference out1 = mock(MDTElementReference.class);
		MDTElementReference out2 = mock(MDTElementReference.class);
		MDTElementReference io1 = mock(MDTElementReference.class);
		IOException boom = new IOException("boom");
		doThrow(boom).when(out1).updateValue(any(ElementValue.class));

		TaskException e = Assertions.assertThrows(TaskException.class, () -> task(out1, out2, io1).run(m_manager));
		// TaskException.getMessage()는 원인을 덧붙이므로 앞부분만 비교한다.
		Assertions.assertTrue(e.getMessage().startsWith("fails to write operation results to arguments: [output:out1]"),
								e.getMessage());
		Assertions.assertSame(boom, e.getCause());

		// 실패한 인자가 있어도 나머지 인자는 모두 기록을 시도한다.
		verify(out2).updateValue(any(ElementValue.class));
		verify(io1).updateValue(any(ElementValue.class));
	}

	@Test
	public void reportsEveryFailure() throws Exception {
		MDTElementReference out1 = mock(MDTElementReference.class);
		MDTElementReference out2 = mock(MDTElementReference.class);
		MDTElementReference io1 = mock(MDTElementReference.class);
		IOException first = new IOException("first");
		IOException second = new IOException("second");
		doThrow(first).when(out1).updateValue(any(ElementValue.class));
		doThrow(second).when(io1).updateValue(any(ElementValue.class));

		TaskException e = Assertions.assertThrows(TaskException.class, () -> task(out1, out2, io1).run(m_manager));
		Assertions.assertTrue(e.getMessage().startsWith(
								"fails to write operation results to arguments: [output:out1, inoutput:io1]"),
								e.getMessage());
		Assertions.assertSame(first, e.getCause());
		Assertions.assertArrayEquals(new Throwable[] { second }, e.getSuppressed());
	}
}
