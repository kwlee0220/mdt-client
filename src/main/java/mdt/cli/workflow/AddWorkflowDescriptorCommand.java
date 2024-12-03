package mdt.cli.workflow;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;
import mdt.model.ResourceAlreadyExistsException;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.WorkflowDescriptor;
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
	description = "Add an MDT Workflow Descriptor."
)
public class AddWorkflowDescriptorCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddWorkflowDescriptorCommand.class);
	
	@Parameters(index="0", paramLabel="path", description="The file path to add.")
	private File m_file;

	@Option(names={"--force", "-f"}, description="remove the existing workflow descriptor if exists")
	private boolean m_force = false;

	public static final void main(String... args) throws Exception {
		main(new AddWorkflowDescriptorCommand(), args);
	}
	
	public AddWorkflowDescriptorCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowDescriptorService svc = ((HttpMDTManagerClient)mdt).createClient(WorkflowDescriptorService.class);
		
		WorkflowDescriptor wfDesc = WorkflowDescriptor.parseJsonFile(m_file);
		while ( true ) {
			try {
				svc.addWorkflowDescriptor(wfDesc);
				break;
			}
			catch ( ResourceAlreadyExistsException e ) {
				if ( m_force ) {
					svc.removeWorkflowDescriptor(wfDesc.getId());
				}
				else {
					throw e;
				}
			}
		}
	}
}
