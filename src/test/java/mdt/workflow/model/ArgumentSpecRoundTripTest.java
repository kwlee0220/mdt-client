package mdt.workflow.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.model.sm.value.PropertyValue;
import mdt.workflow.model.ArgumentSpec.LiteralArgument;
import mdt.workflow.model.ArgumentSpec.TaskOutputArgument;

/**
 * {@link ArgumentSpec}의 공통 속성(식별자/이름/설명)이 JSON 직렬화를 왕복해도 보존되는지 검증한다.
 */
public class ArgumentSpecRoundTripTest {
	@Test
	public void literal_preservesCommonFields() throws IOException {
		LiteralArgument arg = LiteralArgument.builder()
											.id("sleepTime")
											.name("SleepTime")
											.description("대기 시간(초)")
											.value(5)
											.build();

		ArgumentSpec restored = ArgumentSpec.parseJsonString(arg.toJsonString());

		Assertions.assertEquals("sleepTime", restored.getId());
		Assertions.assertEquals("SleepTime", restored.getName());
		Assertions.assertEquals("대기 시간(초)", restored.getDescription());
	}

	@Test
	public void taskOutput_preservesCommonFields() throws IOException {
		TaskOutputArgument arg = TaskOutputArgument.builder()
													.id("result")
													.name("Result")
													.description("이전 태스크의 결과")
													.taskId("task1")
													.argumentName("out1")
													.build();

		ArgumentSpec restored = ArgumentSpec.parseJsonString(arg.toJsonString());

		Assertions.assertEquals("result", restored.getId());
		Assertions.assertEquals("Result", restored.getName());
		Assertions.assertEquals("이전 태스크의 결과", restored.getDescription());
		Assertions.assertEquals("task1", ((TaskOutputArgument)restored).getTaskId());
	}

	@Test
	public void missingRequiredField_isRejectedByBuild() {
		// 식별자는 필수이다.
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> LiteralArgument.builder().value(5).build());
		// 하위 타입의 필수 필드도 마찬가지이다.
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> LiteralArgument.builder().id("noValue").build());
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> TaskOutputArgument.builder().id("x").taskId("task1").build());
	}

	@Test
	public void idIsAlwaysSerialized_whileNameAndDescriptionAreNot() throws IOException {
		LiteralArgument arg = ArgumentSpec.literal("sleepTime", PropertyValue.INTEGER(5));

		String json = arg.toJsonString();
		Assertions.assertTrue(json.contains("\"id\""));
		Assertions.assertFalse(json.contains("\"name\""));
		Assertions.assertFalse(json.contains("\"description\""));

		ArgumentSpec restored = ArgumentSpec.parseJsonString(json);
		Assertions.assertEquals("sleepTime", restored.getId());
		Assertions.assertNull(restored.getName());
		Assertions.assertNull(restored.getDescription());
	}

	@Test
	public void missingIdInJson_isRejected() {
		String json = "{\"@type\":\"mdt:task_arg:task-output\",\"taskId\":\"t\",\"argumentName\":\"a\"}";
		Assertions.assertThrows(IOException.class, () -> ArgumentSpec.parseJsonString(json));
	}
}
