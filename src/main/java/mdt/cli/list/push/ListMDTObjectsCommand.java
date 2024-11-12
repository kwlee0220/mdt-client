package mdt.cli.list.push;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "list",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "List all MDT entities (MDTInstances, AASs, Submodels, Simulations, or AIs)."
)
public class ListMDTObjectsCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTObjectsCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	public static enum TargetType { instance, aas, submodel, simulation, ai };
	
	@Parameters(index="0", arity="0..1", paramLabel="id", description="target type: ${COMPLETION-CANDIDATES}")
	private TargetType m_type = TargetType.instance;
	
	@Option(names={"--filter", "-f"}, description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--table", "-t" }, description="display instances in a table format.")
	private boolean m_tableFormat = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new ListMDTObjectsCommand(), args);
	}
	
	public ListMDTObjectsCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		MDTInstanceManager instMgr = manager.getInstanceManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				ListBuilder builder = switch ( m_type ) {
					case instance -> new ListInstanceCommand(instMgr, m_filter);
					case aas -> new ListAASCommand(instMgr, m_filter);
					case submodel -> new ListSubmodelCommand(instMgr, m_filter);
					case simulation -> new ListSimulationCommand(instMgr, m_filter);
					default -> throw new IllegalArgumentException("Unsupported MDT object type: " + m_type);
				};
				
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				String listStr = (m_tableFormat) ? builder.buildTableString() : builder.buildListString();
				System.out.print(listStr);
			}
			catch ( Exception e ) {
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println("" + e);
			}
			if ( m_verbose ) {
				System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			}
			
			if ( repeatInterval == null ) {
				break;
			}
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
}
