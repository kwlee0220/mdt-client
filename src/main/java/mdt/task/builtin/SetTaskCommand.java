package mdt.task.builtin;	

import java.time.Duration;
import java.time.Instant;

import mdt.model.MDTManager;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "set",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "set task execution command."
)
public class SetTaskCommand extends MultiVariablesCommand {
	@Override
	protected void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setId("set");
		descriptor.setType(SetTask.class.getName());
		
		loadTaskVariablesFromArguments(mdt.getInstanceManager(), descriptor);
		
		SetTask setTask = new SetTask(descriptor);
		setTask.run(mdt.getInstanceManager());
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("HttpTask: elapsedTime={}", elapsed);
		}
	}
	public static void main(String... args) throws Exception {
		main(new SetTaskCommand(), args);
	}
}
