package mdt.cli;

import mdt.task.builtin.AASOperationTaskCommand;
import mdt.task.builtin.HttpTaskCommand;
import mdt.task.builtin.ProgramTaskCommand;
import mdt.task.builtin.SetTaskCommand;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name="run",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="MDT Task related commands",
	subcommands= {
		RunTaskCommand.class,
		AASOperationTaskCommand.class,
		ProgramTaskCommand.class,
		HttpTaskCommand.class,
		SetTaskCommand.class,
	})
public class RunCommands extends CommandCollection {}