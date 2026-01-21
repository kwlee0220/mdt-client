package mdt.cli.workflow;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.model.ResourceAlreadyExistsException;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
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
	description = "Add an MDT Workflow model."
)
public class AddWorkflowModelCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddWorkflowModelCommand.class);
	
	@Parameters(index="0", paramLabel="path", description="The file path to add.")
	private File m_file;

	@Option(names={"--force", "-f"}, description="remove the existing workflow model if exists")
	private boolean m_force = false;

	public static final void main(String... args) throws Exception {
		main(new AddWorkflowModelCommand(), args);
	}
	
	public AddWorkflowModelCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		WorkflowModel wfDesc = WorkflowModel.parseJsonFile(m_file);
		
		
		
		
		
		while ( true ) {
			try {
				wfMgr.addWorkflowModel(wfDesc);
				break;
			}
			catch ( ResourceAlreadyExistsException e ) {
				if ( m_force ) {
					wfMgr.removeWorkflowModel(wfDesc.getId());
				}
				else {
					throw e;
				}
			}
		}
	}
}
