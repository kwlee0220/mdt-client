package mdt.sample;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import mdt.model.expr.MDTExprParser;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.HttpTaskRunner;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleHttpRunner {
	public static final void main(String... args) throws Exception {
		TaskDescriptor descriptor = new TaskDescriptor("test", "", HttpTask.class.getName());
		
		descriptor.addOrReplaceOption(HttpTask.OPTION_SERVER_ENDPOINT, "http://localhost:12987");
		descriptor.addOrReplaceOption(HttpTask.OPTION_OPERATION, "test/AddAndSleep");
		descriptor.addOrReplaceOption(HttpTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOrReplaceOption(HttpTask.OPTION_TIMEOUT, "1m");
		descriptor.addOrReplaceOption(HttpTask.OPTION_SYNC, "false");
		
		descriptor.getInputVariables().add(Variables.newInstance("Data", "",
														ElementReferences.parseExpr("param:test:Data:ParameterValue")));
		descriptor.getInputVariables().add(Variables.newInstance("IncAmount", "",
														MDTExprParser.parseValueLiteral("7").evaluate()));
		descriptor.getInputVariables().add(Variables.newInstance("SleepTime", "",
														MDTExprParser.parseValueLiteral("2.5").evaluate()));
		descriptor.getOutputVariables().add(Variables.newInstance("Output", "",
											ElementReferences.parseExpr("param:test:Data:ParameterValue")));
		
		String jsonStr = JsonMapper.builder()
									.findAndAddModules()
									.addModule(new JavaTimeModule())
									.build()
									.writerFor(TaskDescriptor.class)
									.writeValueAsString(descriptor);
		HttpTaskRunner.main(jsonStr);
	}
}
