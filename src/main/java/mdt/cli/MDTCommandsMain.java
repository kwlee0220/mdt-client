package mdt.cli;

import utils.UsageHelp;

import mdt.cli.MDTCommandsMain.AASCommands;
import mdt.cli.MDTCommandsMain.GetCommands;
import mdt.cli.MDTCommandsMain.KSX9101Commands;
import mdt.cli.MDTCommandsMain.MDTInstanceCommands;
import mdt.cli.MDTCommandsMain.SubmodelCommands;
import mdt.cli.MDTCommandsMain.TaskCommands;
import mdt.cli.MDTCommandsMain.WorkflowCommands;
import mdt.cli.instance.AddMDTInstanceCommand;
import mdt.cli.instance.GetKSX9101Command;
import mdt.cli.instance.GetMDTInstanceCommand;
import mdt.cli.instance.GetPropertyCommand;
import mdt.cli.instance.ListMDTInstanceAllCommand;
import mdt.cli.instance.RemoveMDTInstanceCommand;
import mdt.cli.instance.StartMDTInstanceCommand;
import mdt.cli.instance.StopMDTInstanceCommand;
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
		ListMDTInstanceAllCommand.class,
		GetCommands.class,
		AddMDTInstanceCommand.class,
		RemoveMDTInstanceCommand.class,
		StartMDTInstanceCommand.class,
		StopMDTInstanceCommand.class,
		ShutdownManagerCommand.class,
		
		MDTInstanceCommands.class,
		AASCommands.class,
		SubmodelCommands.class,
		KSX9101Commands.class,
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
		new CommandLine(new MDTCommandsMain())
			.setCaseInsensitiveEnumValuesAllowed(true)
			.setAbbreviatedSubcommandsAllowed(true)
			.execute(args);
		
		System.exit(0);
	}
	
	@Command(
		name="get",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT property related commands",
		subcommands= {
			GetMDTInstanceCommand.class,
			GetPropertyCommand.class,
		})
	public static class GetCommands extends CommandCollection {}
	
	@Command(
		name="instance",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT Instance related commands",
		subcommands= {
			ListMDTInstanceAllCommand.class,
			GetCommands.class,
			AddMDTInstanceCommand.class,
			RemoveMDTInstanceCommand.class,
			StartMDTInstanceCommand.class,
			StopMDTInstanceCommand.class,
		})
	public static class MDTInstanceCommands extends CommandCollection {}
	
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
		name="aas",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="AssetAdministrationShell related commands",
		subcommands= {
			ListAASAllCommand.class,
			GetAASCommand.class,
		})
	public static class AASCommands extends CommandCollection {}
	
	@Command(
		name="submodel",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="AAS Submodel related commands",
		subcommands= {
			ListSubmodelAllCommand.class,
			GetSubmodelCommand.class,
		})
	public static class SubmodelCommands extends CommandCollection {}
	
//	@Command(
//		name="property",
//		parameterListHeading = "Parameters:%n",
//		optionListHeading = "Options:%n",
//		mixinStandardHelpOptions = true,
//		description="MDT property related commands",
//		subcommands= {
//			GetPropertyCommand.class,
//		})
//	public static class PropertyCommands extends CommandCollection {}
	
	@Command(
		name="ksx9101",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="MDT property related commands",
		subcommands= {
			GetKSX9101Command.class,
		})
	public static class KSX9101Commands extends CommandCollection {}
	
	@Command(
		name="simulation",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		mixinStandardHelpOptions = true,
		description="Simulation related commands",
		subcommands= {
			StartSimulationCommand.class,
			StopSimulationCommand.class,
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
