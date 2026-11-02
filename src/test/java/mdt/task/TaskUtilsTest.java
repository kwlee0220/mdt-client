package mdt.task;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.PropertyValue;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.TaskDescriptor;

/**
 * {@link TaskUtils#readArgumentValue}와, 이를 쓰는 RPC 태스크의 입력 처리를 검증한다.
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
}
