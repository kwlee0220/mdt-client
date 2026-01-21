package mdt.cli;

import java.io.File;
import java.time.Duration;

import org.slf4j.LoggerFactory;

import utils.LoggerSettable;
import utils.Picoclies;
import utils.Throwables;
import utils.func.Optionals;

import mdt.client.HttpMDTManager;
import mdt.client.MDTClientConfig;
import mdt.model.MDTManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractMDTCommand implements Runnable, LoggerSettable {
	private org.slf4j.Logger m_logger;
	
	@Option(names={"--client_conf"}, paramLabel="path", required=false,
			description={"MDTManager configuration file path"})
	protected File m_clientConfigFile;
	
	abstract protected void run(MDTManager mdt) throws Exception;
	
	public AbstractMDTCommand() {
		setLogger(LoggerFactory.getLogger(AbstractMDTCommand.class));
	}
	
	@Option(names={"--loglevel"}, paramLabel="logger-level",
					description={"Logger level: debug, info, warn, or error"})
	private Level m_logLevel = null;
//	public void setLoggerLevel(String level) {
//		switch ( level.toLowerCase() ) {
//			case "off":
//				m_logLevel = Level.OFF;
//				break;
//			case "trace":
//				m_logLevel = Level.TRACE;
//				break;
//			case "debug":
//				m_logLevel = Level.DEBUG;
//				break;
//			case "info":
//				m_logLevel = Level.INFO;
//				break;
//			case "warn":
//				m_logLevel = Level.WARN;
//				break;
//			case "error":
//				m_logLevel = Level.ERROR;
//				break;
//			default:
//				throw new IllegalArgumentException("invalid logger level: " + level);
//		}
//	}

	@Override
	public org.slf4j.Logger getLogger() {
		return m_logger;
	}

	@Override
	public void setLogger(org.slf4j.Logger logger) {
		m_logger = Optionals.getOrElse(logger, () -> LoggerFactory.getLogger(AbstractMDTCommand.class));
	}
	
	@Override
	public void run() {
		if ( m_logLevel != null ) {
			Logger root = (Logger)LoggerFactory.getLogger("mdt");
			root.setLevel(m_logLevel);
		}
		
		try {
			HttpMDTManager mdt = connectMDTManager();
			run(mdt);
		}
		catch ( Exception e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			System.err.printf("failed: %s%n", cause);
			System.exit(-1);
		}
	}
	
	private HttpMDTManager connectMDTManager() throws Exception {
		HttpMDTManager mdt;
		
		// 사용자가 명시적으로 client 설정 정보를 지정한 경우에는 이를 통해 MDT Manager에 접속한다.
		if ( m_clientConfigFile != null ) {
			MDTClientConfig config = MDTClientConfig.load(m_clientConfigFile);
			mdt = HttpMDTManager.connect(config);
		}
		// 그렇지 않은 경우는 설정 정보를 사용하거나 환경 변수를 활용하여 MDT Manager에 접속한다.
		else {
			mdt = HttpMDTManager.connectWithDefault();
		}
		getLogger().debug("connecting to MDTInstanceManager {}", mdt.getEndpoint());
		
		return mdt;
	}

	protected static final void main(AbstractMDTCommand cmd, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(cmd)
										.setCaseInsensitiveEnumValuesAllowed(true)
										.setAbbreviatedOptionsAllowed(true)
										.setAbbreviatedSubcommandsAllowed(true)
										.registerConverter(Duration.class, new Picoclies.DurationConverter())
										.registerConverter(Level.class, new Picoclies.LogLevelConverter())
										.setUsageHelpWidth(110);
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
