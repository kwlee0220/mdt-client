package mdt.task;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import utils.func.FOption;

import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.PropertyValue;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;

/**
 * {@link TaskUtils#readArgumentValue}와 이를 쓰는 RPC 태스크의 입력 처리, 그리고
 * {@link TaskUtils#updateLastExecutionTime}의 갱신 대상을 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskUtilsTest {
	@Test
	public void readArgumentValue_literalAndTaskOutput() throws Exception {
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		Assertions.assertEquals(PropertyValue.INTEGER(3),
								TaskUtils.readArgumentValue(ArgumentSpec.literal("a", 3), manager));

		// task-output 인자는 실행 엔진이 정해야 하므로 읽지 않는다.
		// 이전에는 ArgumentSpec.readValue()가 IllegalStateException을 던졌다.
		TaskException e = Assertions.assertThrows(TaskException.class,
						() -> TaskUtils.readArgumentValue(ArgumentSpec.taskOutput("in", "prev", "out"), manager));
		Assertions.assertTrue(e.getMessage().startsWith("task-output argument is not resolved: id=in, source=prev:out"),
								e.getMessage());
	}

	@Test
	public void rpcTasks_failWithTaskExceptionOnUnresolvedTaskOutput() throws IOException {
		TaskDescriptor desc = new TaskDescriptor("t", "", RESTfulRpcTask.class.getName());
		desc.addInputArgument(ArgumentSpec.taskOutput("in", "prev", "out"));
		MDTInstanceManager manager = mock(MDTInstanceManager.class);

		// 입력을 읽는 단계에서 실패하므로 서버에 접속하지 않는다. TaskUtils의 예외가 다시 감싸이지 않는다.
		TaskException e = Assertions.assertThrows(TaskException.class, () -> new RESTfulRpcTask(desc).run(manager));
		Assertions.assertTrue(e.getMessage().startsWith("task-output argument is not resolved"), e.getMessage());
		e = Assertions.assertThrows(TaskException.class, () -> new RESTfulAsyncRpcTask(desc).run(manager));
		Assertions.assertTrue(e.getMessage().startsWith("task-output argument is not resolved"), e.getMessage());
	}

	@Test
	public void updateLastExecutionTime_updatesActivatedReferenceOfSimulation() throws Exception {
		SubmodelService service = mock(SubmodelService.class);
		runUpdateLastExecutionTime(service, Simulation.SEMANTIC_ID_REFERENCE);
		verify(service).updateSubmodelElementValueByPath(eq("SimulationInfo.Model.LastExecutionTime"), anyString());
	}

	@Test
	public void updateLastExecutionTime_updatesActivatedReferenceOfAI() throws Exception {
		SubmodelService service = mock(SubmodelService.class);
		runUpdateLastExecutionTime(service, AI.SEMANTIC_ID_REFERENCE);
		verify(service).updateSubmodelElementValueByPath(eq("AIInfo.Model.LastExecutionTime"), anyString());
	}

	private static void runUpdateLastExecutionTime(SubmodelService service, Reference semanticId) throws Exception {
		when(service.getSubmodel()).thenReturn(new DefaultSubmodel.Builder().semanticId(semanticId).build());
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getSubmodelServiceByIdShort("Op")).thenReturn(FOption.of(service));
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance("inst")).thenReturn(instance);

		TaskDescriptor desc = new TaskDescriptor("t", "", RESTfulRpcTask.class.getName());
		desc.addLabel(TaskDescriptor.LABEL_MDT_OPERATION, "inst:Op");

		// 요소 참조가 활성화되지 않으면 IllegalStateException이 발생한다.
		TaskUtils.updateLastExecutionTime(manager, desc, Instant.now(), LoggerFactory.getLogger(TaskUtilsTest.class));
	}
}

