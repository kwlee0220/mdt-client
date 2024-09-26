package mdt.task.builtin;

import java.io.File;
import java.util.List;

import mdt.task.MDTTaskCommand;

import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "program", description = "run an executable program.")
public class ProgramTaskCommand extends MDTTaskCommand<ProgramTask> {
	@Option(names={"--command"}, paramLabel="command", required=true, split=",",
			description="command line prefix")
	private List<String> m_command;

	@Option(names={"--workingDir"}, paramLabel="dir", required=false,
			description="current working directory")
	private File m_workingDir;

	@Option(names={"--merged-input", "-m"}, paramLabel="input-name", required=false,
			description="the input name for the merged inputs")
	private String m_mergedInputPortName = null;
	
	@Override
	protected ProgramTask newTask() {
		ProgramTask task = new ProgramTask();
		task.setCommand(m_command);
		task.setWorkingDir(m_workingDir);
		task.setMergedInputPortName(m_mergedInputPortName);
		
		return task;
	}

	public static void main(String... args) throws Exception {
		main(new ProgramTaskCommand(), args);
	}
}
