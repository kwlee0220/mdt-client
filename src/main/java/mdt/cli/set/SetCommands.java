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
	description="\nList all MDT-related entities",
	subcommands= {
		SetElementCommand.class,
	})
public class SetCommands extends CommandCollection {
}
