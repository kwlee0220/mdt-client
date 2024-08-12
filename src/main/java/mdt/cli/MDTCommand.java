package mdt.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

import utils.func.FOption;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.MDTInstanceManagerException;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTCommand extends HomeDirPicocliCommand {
	private static final String ENVVAR_HOME = "MDT_CLIENT_HOME";
	private static final String ENVVAR_MDT_ENDPOINT = "MDT_ENDPOINT";
	private static final String CLIENT_CONFIG_FILE = "mdt_client_config.yaml";
	
	@Option(names={"--client_conf"}, paramLabel="path", description={"MDT management configuration file path"})
	protected String m_configPath = CLIENT_CONFIG_FILE;
	
	private Level m_loggerLevel = null;
	
	@Option(names={"--endpoint"}, paramLabel="path", description={"MDTInstanceManager's endpoint"})
	private String m_endpoint = null;
	
	abstract protected void run(MDTClientConfig configs) throws Exception;
	
	public MDTCommand() {
		super(FOption.of(ENVVAR_HOME));
	}
	
	@Option(names={"--level"}, paramLabel="level", description={"Logger level: debug, info, warn, or error"})
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
	
	protected HttpMDTInstanceManagerClient createMDTInstanceManager(MDTClientConfig config) {
		try {
			return HttpMDTInstanceManagerClient.connect(config);
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	@Override
	protected final void run(Path homeDir) throws Exception {
		if ( m_loggerLevel != null ) {
			Logger root = (Logger)LoggerFactory.getLogger("mdt");
			root.setLevel(m_loggerLevel);
		}
		
		Path configPath = Paths.get(m_configPath);
		if ( !configPath.isAbsolute() ) {
			configPath = homeDir.resolve(configPath);
		}
		
		MDTClientConfig config = MDTClientConfig.load(configPath);
		if ( m_endpoint != null ) {
			config.setEndpoint(m_endpoint);
		}
		else if ( config.getEndpoint() == null ) {
			String endpoint = System.getenv(ENVVAR_MDT_ENDPOINT);
			if ( endpoint != null ) {
				config.setEndpoint(endpoint);
			}
		}
		
		run(config);
	}
}
