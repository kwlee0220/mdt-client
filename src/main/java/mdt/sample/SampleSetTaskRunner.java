package mdt.sample;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.variable.Variables;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.SetTask;
import mdt.task.builtin.SetTaskRunner;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleSetTaskRunner {
	public static final void main(String... args) throws Exception {
		TaskDescriptor descriptor = new TaskDescriptor("test", "", HttpTask.class.getName());

//		descriptor.getInputVariables().add(Variables.newInstance(SetTask.VARIABLE_SOURCE, "",
//														ElementReferences.parseExpr("param:test:Data:ParameterValue")));
		descriptor.getInputVariables().add(Variables.newInstance(SetTask.VARIABLE_SOURCE, "",
																ElementValues.parseExpr("123")));
		descriptor.getOutputVariables().add(Variables.newInstance(SetTask.VARIABLE_TARGET, "",
											ElementReferences.parseExpr("oparg:test:AddAndSleep:in:Data")));
		
		List<String> encodedChunks = descriptor.toEncodedString();
		SetTaskRunner.main(encodedChunks.toArray(new String[0]));
		
		List<String> command = Lists.newArrayList("java", "-cp", "mdt-client-all.jar", SetTaskRunner.class.getName());
		command.addAll(encodedChunks);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File("/home/kwlee/mdt/mdt-client"));
		pb.redirectErrorStream(true);
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		pb.start().waitFor();
	}
}
