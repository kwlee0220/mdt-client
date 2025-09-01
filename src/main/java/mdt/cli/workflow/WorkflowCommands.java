package mdt.cli.workflow;

import mdt.cli.CommandCollection;

import picocli.CommandLine.Command;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name="workflow",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="MDT Workflow related commands",
	subcommands= {
		WorkflowModelCommands.class,
		
		ListWorkflowAllCommand.class,
		GetWorkflowCommand.class,
		StartWorkflowCommand.class,
		StopWorkflowCommand.class,
		SuspendWorkflowCommand.class,
		ResumeWorkflowCommand.class,
		RemoveWorkflowCommand.class,
	})
public class WorkflowCommands extends CommandCollection {}