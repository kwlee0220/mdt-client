package mdt.cli.workflow;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.MDTCommand;
import mdt.client.workflow.HttpWorkflowManagerClient;
import mdt.model.AASUtils;
import mdt.model.MDTManager;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.workflow.descriptor.WorkflowDescriptor;
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
public class AddWorkflowDescriptorCommand extends MDTCommand {
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
	public void run(MDTManager manager) throws Exception {
		HttpWorkflowManagerClient client = (HttpWorkflowManagerClient)manager.getWorkflowManager();
		
		WorkflowDescriptor wfDesc = WorkflowDescriptor.parseJsonFile(m_file);
		while ( true ) {
			try {
				client.addWorkflowDescriptor(wfDesc);
				break;
			}
			catch ( ResourceAlreadyExistsException e ) {
				if ( m_force ) {
					client.removeWorkflowDescriptor(wfDesc.getId());
				}
				else {
					throw e;
				}
			}
		}
	}
}
