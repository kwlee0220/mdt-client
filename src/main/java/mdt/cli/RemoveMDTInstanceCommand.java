package mdt.cli;

import java.util.List;

import org.slf4j.Logger;

import utils.LoggerNameBuilder;

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
	description = "Remove the MDT instance."
)
public class RemoveMDTInstanceCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerNameBuilder.from(RemoveMDTInstanceCommand.class).dropSuffix(2)
															.append("unregister.mdt_instances").getLogger();
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance ids to unregister")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="remove all MDTInstances")
	private boolean m_removeAll;
	
	@Option(names={"--force", "-f"}, description="force to remove MDTInstances (eventhough they are running)")
	private boolean m_force;

	public static final void main(String... args) throws Exception {
		main(new RemoveMDTInstanceCommand(), args);
	}

	public RemoveMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		if ( m_removeAll ) {
			manager.removeAllInstances();
		}
		else {
			for ( String instId: m_instanceIds ) {
				manager.removeInstance(instId);
			}
		}
	}
}
