package mdt.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import mdt.client.instance.HttpMDTInstanceManager;
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
public class AddMDTInstanceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddMDTInstanceCommand.class);

	@Parameters(index="0", paramLabel="id", description="MDTInstance implementation jar file path")
	private String m_id;

	@Parameters(index="1", paramLabel="instance-dir", description="Path to the MDTInstance implementation directory")
	private File m_instanceDir;

	@Option(names={"--port", "-p"}, paramLabel="port-number", defaultValue = "-1",
			description="Port number for this MDTInstance. (required only for JarInstance)")
	private int m_port;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new AddMDTInstanceCommand(), args);
	}

	public AddMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		Preconditions.checkArgument(m_instanceDir.isDirectory(),
									"MDTInstance directory path is not a directory: {}", m_instanceDir);
		
		HttpMDTInstanceManager client = (HttpMDTInstanceManager)manager.getInstanceManager();
		client.addInstance(m_id, m_port, m_instanceDir);
		if ( m_verbose ) {
            System.out.printf("added MDTInstance: id=%s, port=%d%n", m_id, m_port);
		}
	}
}
