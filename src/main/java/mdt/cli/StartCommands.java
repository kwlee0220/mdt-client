package mdt.cli;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name="start",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="MDT Task related commands",
	subcommands= {
		StartMDTInstanceCommand.class,
	})
public class StartCommands extends CommandCollection {}