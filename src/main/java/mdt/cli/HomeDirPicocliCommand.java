package mdt.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.LoggerSettable;
import utils.UsageHelp;
import utils.Utilities;
import utils.func.FOption;

import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class HomeDirPicocliCommand implements Runnable, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(HomeDirPicocliCommand.class);
	
	@Spec protected CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	private final FOption<String> m_homeDirEnvVarName;
	private Path m_homeDir = null;
	private Logger m_logger;
	
	protected abstract void run(Path homeDir) throws Exception;
	
	protected HomeDirPicocliCommand(FOption<String> environVarName) {
		m_homeDirEnvVarName = environVarName;
	}
	
	@Override
	public void run() {
		try {
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("use home.dir={}", getHomeDir());
			}
			
			run(getHomeDir());
		}
		catch ( Exception e ) {
			System.err.printf("failed: %s%n%n", e);
		}
	}
	
	public Path getHomeDir() {
		if ( m_homeDir == null ) {
			if ( m_homeDirEnvVarName.isPresent() ) {
				String path = System.getenv(m_homeDirEnvVarName.get());
				if ( path != null ) {
					m_homeDir = Paths.get(path);
				}
			}
		}
		if ( m_homeDir == null ) {
//			if ( m_useWorkingDir ) {
//				m_homeDir = Utilities.getCurrentWorkingDir().toPath();
//			}
//			else {
			m_homeDir = m_homeDirEnvVarName
								.flatMap(env -> FOption.ofNullable(System.getenv(env)))
								.map(Paths::get)
								.getOrElse(Utilities.getUserHomeDir().toPath());
//			}
		}
		
		return m_homeDir;
	}

	@Option(names={"--home"}, paramLabel="path", description={"Home directory"})
	public void setHomeDir(Path path) {
		m_homeDir = path;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	public Path toAbsolutePath(Path path) {
		if ( !path.isAbsolute() ) {
			return getHomeDir().resolve(path).normalize();
		}
		else {
			return path;
		}
	}
	
	public Path toAbsolutePath(String path) {
		return toAbsolutePath(Paths.get(path));
	}

	protected static final void runCommand(HomeDirPicocliCommand cmd, String[] args) throws Exception {
		CommandLine commandLine = new CommandLine(cmd)
										.setUsageHelpWidth(100)
										.setCaseInsensitiveEnumValuesAllowed(true);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				try {
					cmd.run();
				}
				catch ( Exception e ) {
					System.err.println(e);
				}
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
}
