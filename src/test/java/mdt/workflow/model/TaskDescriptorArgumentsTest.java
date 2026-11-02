package mdt.workflow.model;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link TaskDescriptor}의 인자 명세 목록 설정이 식별자 중복을 거부하는지 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskDescriptorArgumentsTest {
	@Test
	public void setters_rejectDuplicateIds() {
		TaskDescriptor task = new TaskDescriptor("t", "", "mdt.task.SetTask");
		// 이전에는 뒤에 오는 같은 식별자가 앞의 것을 조용히 덮어썼다.
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
						() -> task.setInputArguments(List.of(ArgumentSpec.literal("a", 1), ArgumentSpec.literal("a", 2))));
		Assertions.assertEquals("Input argument spec already exists: a", e.getMessage());
		e = Assertions.assertThrows(IllegalArgumentException.class,
						() -> task.setOutputArguments(List.of(ArgumentSpec.reference("o", "param:i:x"),
																ArgumentSpec.reference("o", "param:i:y"))));
		Assertions.assertEquals("Output argument spec already exists: o", e.getMessage());
	}

	@Test
	public void json_rejectsDuplicateIds() {
		String json = "{\"id\":\"t\",\"type\":\"mdt.task.SetTask\",\"inputArguments\":["
					+ "{\"@type\":\"mdt:task_arg:literal\",\"id\":\"a\",\"value\":{\"@type\":\"mdt:value:integer\",\"value\":1}},"
					+ "{\"@type\":\"mdt:task_arg:literal\",\"id\":\"a\",\"value\":{\"@type\":\"mdt:value:integer\",\"value\":2}}]}";
		IOException e = Assertions.assertThrows(IOException.class, () -> TaskDescriptor.parseJsonString(json));
		Assertions.assertTrue(e.getMessage().contains("Input argument spec already exists: a"), e.getMessage());
	}

	@Test
	public void json_roundTripKeepsArguments() throws IOException {
		TaskDescriptor task = new TaskDescriptor("t", "", "mdt.task.SetTask");
		task.addInputArgument(ArgumentSpec.literal("a", 1));
		task.addOutputArgument(ArgumentSpec.reference("o", "param:i:x"));
		TaskDescriptor restored = TaskDescriptor.parseJsonString(task.toJsonString());
		Assertions.assertEquals(List.of("a"), restored.getInputArguments().stream().map(ArgumentSpec::getId).toList());
		Assertions.assertEquals("param:i:x", restored.getOutputArgument("o").getReferenceExpr());
	}
}
