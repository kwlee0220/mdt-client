package mdt.cli.instance;

import java.util.List;

import org.slf4j.Logger;

import utils.LoggerNameBuilder;

import mdt.cli.MDTCommand;
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
	name = "remove",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Unregister the MDT instance."
)
public class RemoveMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerNameBuilder.from(RemoveMDTInstanceCommand.class).dropSuffix(2)
															.append("unregister.mdt_instances").getLogger();
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance ids to unregister")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="remove all MDTInstances")
	private boolean m_removeAll;

	public static final void main(String... args) throws Exception {
		main(new RemoveMDTInstanceCommand(), args);
	}

	public RemoveMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		if ( m_removeAll ) {
			client.removeAllInstances();
		}
		else {
			for ( String instId: m_instanceIds ) {
				client.removeInstance(instId);
			}
		}
	}
}
