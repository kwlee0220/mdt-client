package mdt.task.builtin;

import java.io.File;

import mdt.task.MDTTaskCommand;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTaskCommand extends MDTTaskCommand<SetTask> {
	@ArgGroup(exclusive=true, multiplicity="1")
	private ValueSpec m_value;
	static class ValueSpec {
		@Option(names={"--value"}, paramLabel="Json string", required=true, description="Json string")
		private String m_jsonStr;

		@Option(names={"--file"}, paramLabel="Json file path", required=true, description="Json file path")
		private File m_jsonFile;
	}

	@Override
	protected SetTask newTask() {
		SetTask task = new SetTask();
		task.setJsonStr(m_value.m_jsonStr);
		task.setJsonFile(m_value.m_jsonFile);
		
		return task;
	}

	public static void main(String... args) throws Exception {
		main(new SetTaskCommand(), args);
	}
}
