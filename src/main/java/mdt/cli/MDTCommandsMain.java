package mdt.cli;

import utils.LogbackConfigLoader;

import mdt.cli.MDTCommandsMain.SimulationCommands;
import mdt.cli.MDTCommandsMain.TaskCommands;
import mdt.cli.get.GetCommands;
import mdt.cli.list.ListCommands;
import mdt.cli.set.SetCommands;
import mdt.cli.workflow.WorkflowCommands;
import mdt.task.builtin.AASOperationTaskCommand;
import mdt.task.builtin.HttpTaskCommand;
import mdt.task.builtin.JsltTaskCommand;
import mdt.task.builtin.ProgramTaskCommand;

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
		SetCommands.class,
		TaskCommands.class,
		WorkflowCommands.class,
		SimulationCommands.class,
	})
public class MDTCommandsMain {
	public static final void main(String... args) throws Exception {
		LogbackConfigLoader.loadLogbackConfigFromClass(MDTCommandsMain.class);
		
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
			AASOperationTaskCommand.class,
			ProgramTaskCommand.class,
			HttpTaskCommand.class,
			JsltTaskCommand.class,
		})
	public static class TaskCommands extends CommandCollection {}
	
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
