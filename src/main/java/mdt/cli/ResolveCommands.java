package mdt.cli;

import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */

@Command(
	name="resolve",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="Resolve MDT-related target",
	subcommands= {
		ResolveElementReferenceCommand.class,
	})
public class ResolveCommands extends CommandCollection { }

