package mdt.workflow.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import mdt.model.MDTModelSerDe;
import mdt.workflow.WorkflowModel;

/**
 * {@link TaskDescriptor}의 JSON 직렬화 형식, 알 수 없는 필드 거부, 선행 task 순서 보존을 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskDescriptorJsonTest {
	private static final String BASE = "{\"id\":\"t\",\"type\":\"mdt.task.SetTask\"%s}";

	@Test
	public void unknownField_isRejected() {
		IOException e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString(String.format(BASE, ",\"submodel\":\"x\"")));
		Assertions.assertTrue(e.getMessage().contains("unknown task descriptor field: 'submodel'"), e.getMessage());
	}

	@Test
	public void legacyVariableFields_areRejectedWithHint() {
		IOException e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString(String.format(BASE, ",\"inputVariables\":[]")));
		Assertions.assertTrue(e.getMessage().contains("'inputVariables' (use 'inputArguments')"), e.getMessage());

		e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString(String.format(BASE, ",\"outputVariables\":[]")));
		Assertions.assertTrue(e.getMessage().contains("'outputVariables' (use 'outputArguments')"), e.getMessage());
	}

	@Test
	public void unknownTaskField_failsWorkflowModelParsing() {
		String json = "{\"id\":\"wf\",\"taskDescriptors\":[" + String.format(BASE, ",\"inputVariables\":[]") + "]}";
		Assertions.assertThrows(IOException.class, () -> MDTModelSerDe.readValue(json, WorkflowModel.class));
	}

	@Test
	public void serialization_writesOnlyDescriptorFields() throws IOException {
		TaskDescriptor task = new TaskDescriptor("t", "n", "mdt.task.SetTask");
		task.setDescription("d");
		task.addDependency("a");
		task.setTriggerRule(TriggerRule.ALL_DONE);
		task.addInputArgument(ArgumentSpec.literal("in", 1));
		task.addOutputArgument(ArgumentSpec.reference("out", "param:i:x"));
		task.addOption("loglevel", "info");
		task.addLabel("l", "v");

		JsonNode node = MDTModelSerDe.getJsonMapper().readTree(task.toJsonString());
		List<String> names = fieldNames(node);
		Assertions.assertEquals(List.of("id", "name", "type", "description", "dependencies", "triggerRule",
										"inputArguments", "outputArguments", "options", "labels"), names);
		Assertions.assertEquals("all_done", node.get("triggerRule").asText());
		Assertions.assertTrue(node.get("options").isArray(), node.toString());

		// 직렬화한 결과는 다시 읽을 수 있어야 한다.
		TaskDescriptor restored = TaskDescriptor.parseJsonString(task.toJsonString());
		Assertions.assertEquals(TriggerRule.ALL_DONE, restored.getTriggerRule());
		Assertions.assertEquals("info", restored.findOptionValue("loglevel").getOrNull());
		Assertions.assertEquals("v", restored.findLabel("l").getOrNull());
	}

	@Test
	public void dependencies_keepInsertionOrder() throws IOException {
		List<String> deps = List.of("set_ht", "set_vf", "set_pt", "simulation", "a", "z", "m");

		TaskDescriptor task = new TaskDescriptor("t", "", "mdt.task.SetTask");
		task.addDependency(deps.toArray(String[]::new));
		Assertions.assertEquals(deps, List.copyOf(task.getDependencies()));

		String json = String.format(BASE, ",\"dependencies\":[\"set_ht\",\"set_vf\",\"set_pt\",\"simulation\",\"a\",\"z\",\"m\"]");
		TaskDescriptor read = TaskDescriptor.parseJsonString(json);
		Assertions.assertEquals(deps, List.copyOf(read.getDependencies()));
		Assertions.assertEquals(deps, List.copyOf(TaskDescriptor.parseJsonString(read.toJsonString()).getDependencies()));
	}

	@Test
	public void missingIdOrType_isRejected() {
		IOException e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString("{\"type\":\"mdt.task.SetTask\"}"));
		Assertions.assertTrue(e.getMessage().contains("task id is missing"), e.getMessage());

		e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString("{\"id\":\" \",\"type\":\"mdt.task.SetTask\"}"));
		Assertions.assertTrue(e.getMessage().contains("task id is missing"), e.getMessage());

		e = Assertions.assertThrows(IOException.class, () -> TaskDescriptor.parseJsonString("{\"id\":\"t\"}"));
		Assertions.assertTrue(e.getMessage().contains("task type is missing: task=t"), e.getMessage());

		e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString("{\"id\":\"t\",\"type\":null}"));
		Assertions.assertTrue(e.getMessage().contains("task type is missing: task=t"), e.getMessage());
	}

	@Test
	public void unknownFieldMessage_carriesTaskIdEvenIfItComesFirst() {
		// 알 수 없는 필드가 id보다 앞에 와도 오류 메시지에 task 식별자가 들어가야 한다.
		IOException e = Assertions.assertThrows(IOException.class,
						() -> TaskDescriptor.parseJsonString("{\"bogus\":1,\"id\":\"t\",\"type\":\"mdt.task.SetTask\"}"));
		Assertions.assertTrue(e.getMessage().contains("unknown task descriptor field: 'bogus', task=t"), e.getMessage());
	}

	@Test
	public void options_areReadOnlyView() {
		TaskDescriptor task = new TaskDescriptor("t", "", "mdt.task.SetTask");
		task.addOption("a", "1");
		Assertions.assertThrows(UnsupportedOperationException.class, () -> task.getOptions().remove("a"));
		Assertions.assertThrows(UnsupportedOperationException.class,
								() -> task.getOptions().put("b", new Option("b", "2")));
		Assertions.assertEquals("1", task.findOptionValue("a").getOrNull());
	}

	private static List<String> fieldNames(JsonNode node) {
		List<String> names = new java.util.ArrayList<>();
		for ( Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
			names.add(it.next());
		}
		return names;
	}
}
