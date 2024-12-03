package mdt.cli.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowDescriptorService;
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
	description = "Remove the MDT Workflow Descriptor of the given id."
)
public class RemoveWorkflowDescriptorCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(RemoveWorkflowDescriptorCommand.class);

	@Parameters(index="0..*", paramLabel="ids", description="WorkflowDescriptor ids to remove")
	private List<String> m_wfIds;
	
	@Option(names={"--all", "-a"}, description="remove all WorkflowDescriptor")
	private boolean m_removeAll;

	public static final void main(String... args) throws Exception {
		main(new RemoveWorkflowDescriptorCommand(), args);
	}
	
	public RemoveWorkflowDescriptorCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowDescriptorService svc = ((HttpMDTManagerClient)mdt).createClient(WorkflowDescriptorService.class);

		if ( m_removeAll ) {
			svc.removeWorkflowDescriptorAll();
		}
		else {
			for ( String wfId: m_wfIds ) {
				svc.removeWorkflowDescriptor(wfId);
			}
		}
	}
}
