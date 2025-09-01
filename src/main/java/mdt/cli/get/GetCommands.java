package mdt.cli.get;

import mdt.cli.CommandCollection;
import mdt.cli.get.model.GetModelCommands;

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
		GetInstanceCommand.class,
		GetModelCommands.class,
		GetMdtInfoCommand.class,
		GetElementCommand.class,
		GetShellCommand.class,
		GetSubmodelCommand.class,
		GetFile.class,
	})
public class GetCommands extends CommandCollection { }

