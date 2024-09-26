package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.SetTaskCommand;

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
	description = "SubmodelElement-value-update task execution command."
)
public class SetTaskLauncher extends SetTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(SetTaskLauncher.class);
	
	public SetTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		main(new SetTaskLauncher(), args);
	}
}
