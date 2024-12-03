package mdt.cli.get;

import mdt.cli.CommandCollection;
import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */

@Command(
	name="get",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="Show MDT-related entity's properties",
	subcommands= {
		GetMDTInstanceCommand.class,
		GetSubmodelElementCommand.class,
		GetShellCommand.class,
		GetSubmodelCommand.class,
		GetKSX9101Command.class,
	})
public class GetCommands extends CommandCollection { }

