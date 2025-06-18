package mdt.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Parameters(index="1", paramLabel="path",
				description="Path to the MDTInstance implementation directory or zip file")
	private File m_instanceFile;
	
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
		HttpMDTInstanceManager client = (HttpMDTInstanceManager)manager.getInstanceManager();
		client.addInstance(m_id, m_instanceFile);
		if ( m_verbose ) {
            System.out.printf("added MDTInstance: id=%s%n", m_id);
		}
	}
}
