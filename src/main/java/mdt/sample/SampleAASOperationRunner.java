package mdt.sample;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import mdt.model.expr.MDTExprParser;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.AASOperationTaskRunner;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleAASOperationRunner {
	public static final void main(String... args) throws Exception {
		TaskDescriptor descriptor = new TaskDescriptor("test", "", AASOperationTask.class.getName());
		
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_OPERATION,
							ElementReferences.parseExpr("test:AddAndSleep:Operation"));
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_POLL_INTERVAL, "1s");
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_TIMEOUT, "1m");
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_SYNC, ""+false);
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_UPDATE_OPVARS, ""+true);
		
		descriptor.getInputVariables().add(Variables.newInstance("Data", "",
														ElementReferences.parseExpr("param:test:Data:ParameterValue")));
		descriptor.getInputVariables().add(Variables.newInstance("IncAmount", "",
														MDTExprParser.parseValueLiteral("7").evaluate()));
		descriptor.getInputVariables().add(Variables.newInstance("SleepTime", "",
														MDTExprParser.parseValueLiteral("3").evaluate()));
		descriptor.getOutputVariables().add(Variables.newInstance("Output", "",
											ElementReferences.parseExpr("param:test:Data:ParameterValue")));
		
		String jsonStr = JsonMapper.builder()
									.findAndAddModules()
									.addModule(new JavaTimeModule())
									.build()
									.writerFor(TaskDescriptor.class)
									.writeValueAsString(descriptor);
		AASOperationTaskRunner.main(jsonStr);
	}
}
