package mdt.workflow.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link TaskDescriptor}의 trigger rule 직렬화를 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskDescriptorTriggerRuleTest {
	private static TaskDescriptor newTask() {
		TaskDescriptor task = new TaskDescriptor("return_home", "홈 복귀", "mdt.task.AASOperationTask");
		task.addDependency("place");
		return task;
	}

	@Test
	public void defaultRuleIsAllSuccessAndNotWritten() throws IOException {
		TaskDescriptor task = newTask();
		Assertions.assertEquals(TriggerRule.ALL_SUCCESS, task.getTriggerRule());
		// 기본값은 기록하지 않아 기존 워크플로 JSON과 같은 결과가 나와야 한다.
		Assertions.assertFalse(task.toJsonString().contains("triggerRule"), task.toJsonString());
	}

	@Test
	public void explicitRuleIsWrittenOnceAfterDependencies() throws IOException {
		TaskDescriptor task = newTask();
		task.setTriggerRule(TriggerRule.NONE_FAILED_MIN_ONE_SUCCESS);

		String json = task.toJsonString();
		Assertions.assertTrue(json.contains("\"triggerRule\":\"none_failed_min_one_success\""), json);
		Assertions.assertEquals(json.indexOf("\"triggerRule\""), json.lastIndexOf("\"triggerRule\""), json);
		Assertions.assertTrue(json.indexOf("\"dependencies\"") < json.indexOf("\"triggerRule\""), json);
		Assertions.assertTrue(json.indexOf("\"triggerRule\"") < json.indexOf("\"inputArguments\""), json);
	}

	@Test
	public void everyRuleRoundTrips() throws IOException {
		for ( TriggerRule rule: TriggerRule.values() ) {
			TaskDescriptor task = newTask();
			task.setTriggerRule(rule);
			TaskDescriptor read = TaskDescriptor.parseJsonString(task.toJsonString());
			Assertions.assertEquals(rule, read.getTriggerRule());
		}
	}

	@Test
	public void missingOrNullRuleReadsAsDefault() throws IOException {
		String base = "{\"id\":\"t1\",\"type\":\"mdt.task.AASOperationTask\"%s}";
		Assertions.assertEquals(TriggerRule.ALL_SUCCESS,
								TaskDescriptor.parseJsonString(String.format(base, "")).getTriggerRule());
		Assertions.assertEquals(TriggerRule.ALL_SUCCESS,
								TaskDescriptor.parseJsonString(String.format(base, ",\"triggerRule\":null")).getTriggerRule());
		Assertions.assertEquals(TriggerRule.ALL_DONE,
								TaskDescriptor.parseJsonString(String.format(base, ",\"triggerRule\":\"all_done\"")).getTriggerRule());
	}

	@Test
	public void unsupportedRuleIsRejectedWhenParsing() {
		String json = "{\"id\":\"t1\",\"type\":\"mdt.task.AASOperationTask\",\"triggerRule\":\"one_success\"}";
		IOException e = Assertions.assertThrows(IOException.class, () -> TaskDescriptor.parseJsonString(json));
		Assertions.assertTrue(e.getMessage().contains("unsupported trigger rule: one_success"), e.getMessage());
	}

	@Test
	public void nameLookupMatchesAirflowNames() {
		Assertions.assertEquals("all_success", TriggerRule.ALL_SUCCESS.getName());
		Assertions.assertEquals(TriggerRule.ONE_FAILED, TriggerRule.fromName("one_failed"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> TriggerRule.fromName("ALL_SUCCESS"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> newTask().setTriggerRule(null));
	}
}
