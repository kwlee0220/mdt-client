package mdt.cli;

import java.nio.file.Path;

import org.slf4j.LoggerFactory;

import utils.HomeDirPicocliCommand;
import utils.func.FOption;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import mdt.client.HttpMDTManagerClient;
import mdt.client.MDTClientException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManagerException;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTCommand extends HomeDirPicocliCommand {
	private static final String ENVVAR_HOME = "MDT_CLIENT_HOME";
	private static final String ENVVAR_MDT_ENDPOINT = "MDT_ENDPOINT";
	
	private Level m_loggerLevel = null;
	
	@Option(names={"--endpoint"}, paramLabel="path", description={"MDTInstanceManager's endpoint"})
	private String m_endpoint = null;
	
	abstract protected void run(MDTManager manager) throws Exception;
	
	public MDTCommand() {
		super(ENVVAR_HOME);
		
		setLogger(LoggerFactory.getLogger(MDTCommand.class));
	}
	
	@Option(names={"--logger"}, paramLabel="logger-level",
					description={"Logger level: debug, info, warn, or error"})
	public void setLoggerLevel(String level) {
		switch ( level.toLowerCase() ) {
			case "off":
				m_loggerLevel = Level.OFF;
				break;
			case "trace":
				m_loggerLevel = Level.TRACE;
				break;
			case "debug":
				m_loggerLevel = Level.DEBUG;
				break;
			case "info":
				m_loggerLevel = Level.INFO;
				break;
			case "warn":
				m_loggerLevel = Level.WARN;
				break;
			case "error":
				m_loggerLevel = Level.ERROR;
				break;
			default:
				throw new IllegalArgumentException("invalid logger level: " + level);
		}
	}
	
	@Override
	protected final void run(Path homeDir) throws Exception {
		if ( m_loggerLevel != null ) {
			Logger root = (Logger)LoggerFactory.getLogger("mdt");
			root.setLevel(m_loggerLevel);
		}
		
		String endpoint = FOption.getOrElse(m_endpoint, () -> System.getenv(ENVVAR_MDT_ENDPOINT));
		if ( endpoint == null ) {
			throw new IllegalStateException("MDTInstanceManager's endpoint is missing");
		}
		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("connecting to MDTInstanceManager {}", endpoint);
		}
		
		try {
			HttpMDTManagerClient manager = HttpMDTManagerClient.connect(endpoint);
			run(manager);
		}
		catch ( MDTClientException e ) {
			throw e;
		}
		catch ( RuntimeException e ) {
			throw e;
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}

	protected static final void main(MDTCommand cmd, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(100);
		try {
			commandLine.parseArgs(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				cmd.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
}
