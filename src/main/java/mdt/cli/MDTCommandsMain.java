package mdt.cli;

import java.util.List;

import utils.UsageHelp;

import mdt.cli.MDTCommandsMain.GetCommand;
import mdt.cli.MDTCommandsMain.SimulationCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.DefaultExceptionHandler;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.RunLast;
import picocli.CommandLine.Spec;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name="mdt",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		description="Manufactoring DigitalTwin (MDT) related commands",
		subcommands = {
			ListMDTInstanceAllCommand.class,
			GetCommand.class,
			StartMDTInstanceCommand.class,
			SimulationCommand.class,
			AddMDTInstanceCommand.class,
			RemoveMDTInstanceCommand.class,
			StopMDTInstanceCommand.class,
		})
public class MDTCommandsMain implements Runnable {
	@Spec private CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	@Override
	public void run() {
		m_spec.commandLine().usage(System.out, Ansi.OFF);
	}

	public static final void main(String... args) throws Exception {
		MDTCommandsMain main = new MDTCommandsMain();
		CommandLine cmd = new CommandLine(main).setCaseInsensitiveEnumValuesAllowed(true);
        cmd.parseWithHandlers(new RunLast().useOut(System.out).useAnsi(Help.Ansi.OFF),
        						new DefaultExceptionHandler<List<Object>>().useErr(System.err)
        																	.useAnsi(Help.Ansi.OFF),
        						args);
		
//		CommandLine.run(main, System.out, System.err, Help.Ansi.OFF, args);
	}

	@Command(name="get",
			description="MDT Instance related commands",
			subcommands= {
				GetMDTInstanceCommand.class,
				GetAASCommand.class,
				GetSubmodelCommand.class,
				GetPropertyCommand.class,
				GetKSX9101Command.class,
			})
	public static class GetCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="simulation",
			description="MDT Simulation related commands",
			subcommands= {
				StartSimulationCommand.class,
				CancelSimulationCommand.class,
			})
	public static class SimulationCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="aas",
			description="AssetAdministrationShell related commands",
			subcommands= {
				ListAASAllCommand.class,
			})
	public static class AASCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="submodel",
			description="Submodel related commands",
			subcommands= {
				ListSubmodelAllCommand.class,
			})
	public static class SubmodelCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="ksx9101",
			description="KSX 9101 Property related commands",
			subcommands= {
				GetKSX9101Command.class,
			})
	public static class KSX9101Command implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}
}
