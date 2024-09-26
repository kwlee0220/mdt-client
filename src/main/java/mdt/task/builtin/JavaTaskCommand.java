package mdt.task.builtin;

import mdt.task.MDTTaskCommand;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@picocli.CommandLine.Command(name = "java", description = "run Java class task.")
public class JavaTaskCommand extends MDTTaskCommand<JavaTask> {
	@Option(names={"--class"}, paramLabel="java-class-name", required=true,
			description="the fully-qualified-class-name of the target class")
	private String m_taskModuleClassName;
	
	@Override
	protected JavaTask newTask() {
		JavaTask task = new JavaTask();
		task.setTaskModuleClassName(m_taskModuleClassName);
		
		return task;
	}

	public static final void main(String... args) throws Exception {
		main(new JavaTaskCommand(), args);
	}
}
