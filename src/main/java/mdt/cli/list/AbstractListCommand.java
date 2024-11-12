package mdt.cli.list;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;

import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractListCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractListCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	private MDTInstanceManager m_manager;
	
	@Option(names={"--table", "-t" }, description="display instances in a table format.")
	private boolean m_tableFormat = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	abstract public String buildTableString();
	abstract public String buildListString();
	
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
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				if 
				( m_tableFormat ) {
					String listStr = buildTableString();
					System.out.print(listStr);
					System.out.println();
				}
				else {
					String listStr = buildListString();
					System.out.print(listStr);
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
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
}
