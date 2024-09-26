package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.ProgramTaskCommand;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "program",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Program task execution command."
)
public class ProgramTaskLauncher extends ProgramTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTaskLauncher.class);
	
	public ProgramTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		main(new ProgramTaskLauncher(), args);
	}
}
