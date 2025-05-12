package mdt.cli.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "stop",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Stop an MDT Workflow."
)
public class StopWorkflowCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopWorkflowCommand.class);
	
	@Parameters(index="0", paramLabel="workflow-name", description="Workflow model id to stop")
	private String m_wfName;

	public static final void main(String... args) throws Exception {
		main(new StopWorkflowCommand(), args);
	}
	
	public StopWorkflowCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		wfMgr.stopWorkflow(m_wfName);
	}
}
