package mdt.cli.list;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;

import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractListCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractListCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	private MDTInstanceManager m_manager;
	
	@Option(names={"--table", "-t" }, description="display instances in a table format.")
	private boolean m_tableFormat = false;
	
	@Option(names={"--tree", "-T" }, description="display instances in a table format.")
	private boolean m_showAsTree = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	abstract public String buildTableString();
	abstract public String buildListString();
	abstract public String buildTreeString();
	
	protected AbstractListCommand() {
		setLogger(s_logger);
	}
	
	public MDTInstanceManager getMDTInstanceManager() {
		return m_manager;
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		m_manager = mdt.getInstanceManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				String listStr;
				if ( m_showAsTree ) {
					listStr = buildTreeString();
				}
				else if ( m_tableFormat ) {
					listStr = buildTableString();
				}
				else {
					listStr = buildListString();
				}
				
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.print(listStr);
				if ( m_tableFormat ) {
					System.out.println();
				}
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
			
			long remainMillis = repeatInterval.minus(watch.getElapsed()).toMillis();
			if ( remainMillis > 0 ) {
				TimeUnit.MILLISECONDS.sleep(remainMillis);
			}
		}
	}
}
