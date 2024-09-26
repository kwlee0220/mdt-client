package mdt.task.builtin;

import mdt.task.MDTTaskCommand;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "copy", description = "copy SubmodelElement")
public class CopyTaskCommand extends MDTTaskCommand<CopyTask> {
	@Override
	protected CopyTask newTask() {
		CopyTask task = new CopyTask();
		
		return task;
	}

	public static void main(String... args) throws Exception {
		main(new CopyTaskCommand(), args);
	}
}
