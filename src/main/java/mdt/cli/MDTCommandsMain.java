package mdt.cli;

import java.time.Duration;

import utils.LogbackConfigLoader;
import utils.Picoclies;

import mdt.cli.MDTCommandsMain.SimulationCommands;
import mdt.cli.get.GetCommands;
import mdt.cli.list.ListCommands;
import mdt.cli.remove.RemoveCommands;
import mdt.cli.set.SetCommands;
import mdt.cli.workflow.WorkflowCommands;

import ch.qos.logback.classic.Level;
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
		BuildInstanceCommand.class,
		ListCommands.class,
		GetCommands.class,
		RemoveCommands.class,
		AddMDTInstanceCommand.class,	
//		RemoveMDTInstanceCommand.class,	
		StartMDTInstanceCommand.class,
		StopMDTInstanceCommand.class,
		SetCommands.class,
		RunCommands.class,
		WorkflowCommands.class,
		ResolveCommands.class,
		SimulationCommands.class,
	})
public class MDTCommandsMain {
	public static final void main(String... args) throws Exception {
		LogbackConfigLoader.loadLogbackConfigFromClass(MDTCommandsMain.class);
		
		CommandLine cmdLine = new CommandLine(new MDTCommandsMain())
									.setCaseInsensitiveEnumValuesAllowed(true)
									.setAbbreviatedOptionsAllowed(true)
									.setAbbreviatedSubcommandsAllowed(true)
									.registerConverter(Duration.class, new Picoclies.DurationConverter())
									.registerConverter(Level.class, new Picoclies.LogLevelConverter())
									.setUsageHelpWidth(110);
		System.exit(cmdLine.execute(args));
	}
	
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
