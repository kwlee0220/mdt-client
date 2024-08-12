package mdt.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "add", description = "Add an MDT instance.")
public class AddMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddMDTInstanceCommand.class);
	
	public static enum InstanceType {
		JAR,
		DOCKER,
		KUBERNETES,
	}

	@Parameters(index="0", paramLabel="id", description="MDTInstance implementation jar file path")
	private String m_id;
	
	@Option(names={"--type", "-t"}, paramLabel="type",
			description="MDTInstance execution type: ${COMPLETION-CANDIDATES}")
	private InstanceType m_type;
	
	@Option(names={"--jar"}, paramLabel="path",
			description="Path to the MDTInstance implementation jar (for JarInstance)")
	private File m_jarFile;
	
	@Option(names={"--image"}, paramLabel="id",
			description="Docker image id for the MDTInstance (DockerInstance or KubernetesInstance)")
	private String m_imageId;
	
	@Option(names={"--model", "-m"}, paramLabel="path", description="Initial AAS Environment file path")
	private File m_aasFile;
	
	@Option(names={"--conf", "-c"}, paramLabel="path", description="MDTInstance configuration path")
	private File m_aasConf;

	public AddMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mgr = (HttpMDTInstanceManagerClient)createMDTInstanceManager(configs);
		mgr.addInstance(m_id, m_imageId, m_jarFile, m_aasFile, m_aasConf);
	}

	public static final void main(String... args) throws Exception {
		runCommand(new AddMDTInstanceCommand(), args);
	}
}
