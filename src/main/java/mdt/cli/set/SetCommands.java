package mdt.cli.set;

import mdt.cli.CommandCollection;

import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */

@Command(
	name="set",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="\nUpdate MDT entity",
	subcommands= {
		SetElementCommand.class,
		SetParameterCommand.class,
		SetArgumentCommand.class,
	})
public class SetCommands extends CommandCollection {
}
