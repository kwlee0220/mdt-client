package mdt.cli;

import utils.UsageHelp;

import mdt.cli.MDTCommandsMain.SimulationCommands;
import mdt.cli.MDTCommandsMain.TaskCommands;
import mdt.cli.MDTCommandsMain.WorkflowCommands;
import mdt.cli.get.GetCommands;
import mdt.cli.list.ListCommands;
import mdt.cli.task.AASOperationTaskLauncher;
import mdt.cli.task.CopyTaskLauncher;
import mdt.cli.task.HttpTaskLauncher;
import mdt.cli.task.JsltTaskLauncher;
import mdt.cli.task.ProgramTaskLauncher;
import mdt.cli.task.SetTaskLauncher;
import mdt.cli.workflow.AddWorkflowDescriptorCommand;
import mdt.cli.workflow.ConvertWorkflowDescriptorCommand;
import mdt.cli.workflow.GetWorkflowDescriptorCommand;
import mdt.cli.workflow.ListWorkflowDescriptorAllCommand;
import mdt.cli.workflow.RemoveWorkflowDescriptorCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name="mdt",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description="%nManufactoring DigitalTwin (MDT) related commands.",
	subcommands = {
		EndpointCommand.class,
		ListCommands.class,
		GetCommands.class,
		AddMDTInstanceCommand.class,	
		RemoveMDTInstanceCommand.class,	
		StartMDTInstanceCommand.class,
		StopMDTInstanceCommand.class,
		SimulationCommands.class,
		TaskCommands.class,
		WorkflowCommands.class,
	})
public class MDTCommandsMain implements Runnable {
	@Spec private CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	@Override
	public void run() {
		m_spec.commandLine().usage(System.out, Ansi.OFF);
	}

	public static final void main(String... args) throws Exception {
		CommandLine cmdLine = new CommandLine(new MDTCommandsMain())
									.setCaseInsensitiveEnumValuesAllowed(true)
									.setAbbreviatedSubcommandsAllowed(true)
									.setAbbreviatedOptionsAllowed(true)
									.setUsageHelpWidth(110);
		cmdLine.execute(args);
		
		System.exit(0);
	}
	
	@Command(
		name="task",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT Task related commands",
		subcommands= {
			SetTaskLauncher.class,
			CopyTaskLauncher.class,
			AASOperationTaskLauncher.class,
			ProgramTaskLauncher.class,
			HttpTaskLauncher.class,
			JsltTaskLauncher.class,
		})
	public static class TaskCommands extends CommandCollection {}
	
	@Command(
		name="workflow",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT WorkflowDescriptor related commands",
		subcommands= {
			ListWorkflowDescriptorAllCommand.class,
			GetWorkflowDescriptorCommand.class,
			AddWorkflowDescriptorCommand.class,
			RemoveWorkflowDescriptorCommand.class,
			ConvertWorkflowDescriptorCommand.class,
		})
	public static class WorkflowCommands extends CommandCollection {}
	
	@Command(
		name="simulation",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="Simulation related commands",
		subcommands= {
			StartSkkuSimulationCommand.class,
		})
	public static class SimulationCommands extends CommandCollection {}
	
	@Command(
		name="manager",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDTManager related commands",
		subcommands= {
			ShutdownManagerCommand.class,
		})
	public static class MDTManagerCommands extends CommandCollection {}
}
