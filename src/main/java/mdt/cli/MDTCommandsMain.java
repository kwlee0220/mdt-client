package mdt.cli;

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
import mdt.cli.workflow.GetWorkflowDescriptorCommand;
import mdt.cli.workflow.ListWorkflowDescriptorAllCommand;
import mdt.cli.workflow.RemoveWorkflowDescriptorCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;

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
		SetTaskLauncher.class,
		CopyTaskLauncher.class,
		ExportMDTFramework.class,
		TaskCommands.class,
		WorkflowCommands.class,
		SimulationCommands.class,
	})
public class MDTCommandsMain {
	public static final void main(String... args) throws Exception {
		CommandLine cmdLine = new CommandLine(new MDTCommandsMain())
									.setCaseInsensitiveEnumValuesAllowed(true)
									.setAbbreviatedSubcommandsAllowed(true)
									.setAbbreviatedOptionsAllowed(true)
									.setUsageHelpWidth(110);
		System.exit(cmdLine.execute(args));
	}
	
	@Command(
		name="run",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT Task related commands",
		subcommands= {
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
