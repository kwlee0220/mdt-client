package mdt.cli.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "start",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Start an MDT Workflow."
)
public class StartWorkflowCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StartWorkflowCommand.class);
	
	@Parameters(index="0", paramLabel="template-id", description="Workflow template id to start")
	private String m_wfTemplateId;

	public static final void main(String... args) throws Exception {
		main(new StartWorkflowCommand(), args);
	}
	
	public StartWorkflowCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		Workflow wf = wfMgr.startWorkflow(m_wfTemplateId);
		System.out.println(wf);
	}
}
