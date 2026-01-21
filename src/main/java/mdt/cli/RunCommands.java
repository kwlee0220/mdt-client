package mdt.cli;

import mdt.task.builtin.RunSubmodelCommand;
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
		RunAASOperationCommand.class,
		SetTaskCommand.class,
		RunSubmodelCommand.class,
	})
public class RunCommands extends CommandCollection {}