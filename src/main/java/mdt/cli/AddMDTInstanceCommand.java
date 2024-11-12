package mdt.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "add",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Add an MDT instance."
)
public class AddMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddMDTInstanceCommand.class);
	
	public static enum InstanceType {
		JAR,
		DOCKER,
		KUBERNETES,
	}

	@Parameters(index="0", paramLabel="id", description="MDTInstance implementation jar file path")
	private String m_id;
	
	@Option(names={"--jar", "-j"}, paramLabel="path",
			description="Path to the MDTInstance implementation jar (for JarInstance)")
	private File m_jarFile;

	@Option(names={"--port", "-p"}, paramLabel="port-number", defaultValue = "-1",
			description="Port number for this MDTInstance. (required only for JarInstance)")
	private int m_port;
	
	@Option(names={"--model", "-m"}, paramLabel="path", defaultValue="model.aasx",
			description="Initial AAS Environment file path")
	private File m_aasFile;
	
	@Option(names={"--conf", "-c"}, paramLabel="path", defaultValue="config.json",
			description="MDTInstance configuration path")
	private File m_aasConf;

	public static final void main(String... args) throws Exception {
		runCommand(new AddMDTInstanceCommand(), args);
	}

	public AddMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		client.addInstance(m_id, m_port, m_jarFile, m_aasFile, m_aasConf);
	}
}
