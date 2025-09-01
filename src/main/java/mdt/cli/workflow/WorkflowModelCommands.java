package mdt.cli.workflow;

import mdt.cli.CommandCollection;

import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name="model",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="MDT Workflow related commands",
	subcommands= {
		ListWorkflowModelAllCommand.class,
		GetWorkflowModelCommand.class,
		AddWorkflowModelCommand.class,
		RemoveWorkflowModelCommand.class,
	})
public class WorkflowModelCommands extends CommandCollection {}