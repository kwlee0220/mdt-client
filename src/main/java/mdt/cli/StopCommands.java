package mdt.cli;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name="stop",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="Stop commands",
	subcommands= {
		StopMDTInstanceCommand.class,
	})
public class StopCommands extends CommandCollection {}