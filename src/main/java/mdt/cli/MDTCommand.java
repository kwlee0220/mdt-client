package mdt.cli;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.LoggerFactory;

import utils.HomeDirPicocliCommand;
import utils.http.RESTfulIOException;
import utils.http.RESTfulRemoteException;

import mdt.client.HttpMDTManagerClient;
import mdt.client.MDTClientConfig;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.task.TaskException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTCommand extends HomeDirPicocliCommand {
	private static final String ENVVAR_HOME = "MDT_CLIENT_HOME";
	
	private Level m_loggerLevel = null;
	
	@Option(names={"--client_conf"}, paramLabel="path", required=false,
			description={"MDTManager configuration file path"})
	protected File m_clientConfigFile;
	
	@Option(names={"--endpoint"}, paramLabel="path", required=false, description={"MDTInstanceManager's endpoint"})
	protected String m_endpoint = null;
	
	abstract protected void run(MDTManager mdt) throws Exception;
	
	public MDTCommand() {
		super(ENVVAR_HOME);
		
		setLogger(LoggerFactory.getLogger(MDTCommand.class));
	}
	
	@Option(names={"--loglevel"}, paramLabel="logger-level",
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
	protected void run(Path homeDir) throws Exception {
		if ( m_loggerLevel != null ) {
			Logger root = (Logger)LoggerFactory.getLogger("mdt");
			root.setLevel(m_loggerLevel);
		}

		HttpMDTManagerClient mdt;

		// 사용자가 명시적으로 endpoint를 지정한 경우에는 이를 통해 MDT Manager에 접속한다.
		if ( m_endpoint != null ) {
			mdt = HttpMDTManagerClient.connect(m_endpoint);
		}
		// 사용자가 명시적으로 client 설정 정보를 지정한 경우에는 이를 통해 MDT Manager에 접속한다.
		else if ( m_clientConfigFile != null ) {
			MDTClientConfig config = MDTClientConfig.load(m_clientConfigFile);
			mdt = HttpMDTManagerClient.connect(config);
		}
		// 그렇지 않은 경우는 설정 정보를 사용하거나 환경 변수를 활용하여 MDT Manager에 접속한다.
		else {
			mdt = HttpMDTManagerClient.connectWithDefault();
		}
		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("connecting to MDTInstanceManager {}", mdt.getEndpoint());
		}
		
		try {
			run(mdt);
		}
		catch ( TaskException e ) {
			throw e;
		}
		catch ( RESTfulRemoteException | RESTfulIOException e ) {
			throw e;
		}
		catch ( RuntimeException e ) {
			throw e;
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException(e);
		}
	}

	protected static final void main(MDTCommand cmd, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(110);
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
