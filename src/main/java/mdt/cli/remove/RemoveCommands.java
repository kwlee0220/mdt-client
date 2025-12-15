package mdt.cli.remove;

import mdt.cli.CommandCollection;

import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */

@Command(
	name="remove",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="\nRemove MDT entity",
	subcommands= {
		RemoveMDTInstanceCommand.class,
		RemoveFileCommand.class,
	})
public class RemoveCommands extends CommandCollection {
}
