package mdt.cli.get;

import mdt.cli.CommandCollection;
import mdt.cli.get.instance.GetInstanceCommand;

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
		GetMdtInfoCommand.class,
		
		GetElementCommand.class,
		GetParameterCommand.class,
		GetArgumentCommand.class,
		
		GetShellCommand.class,
		GetSubmodelCommand.class,
		GetFile.class,
		
		GetTimeSeriesCommand.class,
	})
public class GetCommands extends CommandCollection { }

