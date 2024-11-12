package mdt.cli;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Utilities;
import utils.io.FileUtils;

import mdt.client.MDTClientConfig;
import mdt.model.MDTManager;

import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "endpoint",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get the endpoint for the client."
)
public class EndpointCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(EndpointCommand.class);
	private static final String CLIENT_CONFIG_FILE = "mdt_client_config.yaml";

	public static final void main(String... args) throws Exception {
		main(new EndpointCommand(), args);
	}

	public EndpointCommand() {
		setLogger(s_logger);
	}
	
	@Override
	protected final void run(Path homeDir) throws Exception {
		// 사용자가 명시적으로 client 설정 정보를 지정한 경우에는 이를 통해 MDT Manager에 접속한다.
		if ( m_clientConfigFile != null ) {
			MDTClientConfig config = MDTClientConfig.load(m_clientConfigFile);
			System.out.println(config.getEndpoint() + " (from option '--client_conf'");
			System.exit(0);
		}
		
		// 사용자가 명시적으로 endpoint를 지정한 경우에는 이를 통해 MDT Manager에 접속한다.
		if ( m_endpoint != null ) {
			System.out.println(m_endpoint + " (from option '--endpoint')");
			System.exit(0);
		}
		
		// 그렇지 않은 경우는 설정 정보를 사용하거나 환경 변수를 활용하여 MDT Manager에 접속한다.
		File clientHomeDir = Utilities.getEnvironmentVariableFile("MDT_CLIENT_HOME")
										.getOrElse(FileUtils.getCurrentWorkingDirectory());
		File clientConfigFile = FileUtils.path(clientHomeDir, CLIENT_CONFIG_FILE);
		if ( clientConfigFile.canRead() ) {
			try {
				MDTClientConfig config = MDTClientConfig.load(clientConfigFile);
				System.out.println(config.getEndpoint()
									+ " (from configuration file: " + clientConfigFile.getAbsolutePath() + ")");
				System.exit(0);
			}
			catch ( Throwable expected ) { }
		}

		// client config file이 존재하지 않는 경우에는 환경변수 MDT_ENDPOINT에 기록된
		// endpoint 정보를 사용하여 접속을 시도한다.
		String endpoint = System.getenv("MDT_ENDPOINT");
		if ( endpoint == null ) {
			System.out.println(endpoint + " (from environment variable 'MDT_ENDPOINT')");
			System.exit(0);
		}
		
		System.err.println("Cannot get MDTManager's endpoint");
		System.exit(-1);
	}

	@Override
	protected void run(MDTManager mdt) throws Exception {
		throw new AssertionError();
	}
}
