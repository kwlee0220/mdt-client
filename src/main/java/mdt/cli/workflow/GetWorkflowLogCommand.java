package mdt.cli.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "log",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an MDT Workflow log."
)
public class GetWorkflowLogCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetWorkflowLogCommand.class);
	
	@ParentCommand GetWorkflowCommand m_parent;
	
	@Option(names={"--pod"}, paramLabel="name", required=true, description="Pod name to read log file")
	private String m_podName;

	public static final void main(String... args) throws Exception {
		main(new GetWorkflowLogCommand(), args);
	}
	
	public GetWorkflowLogCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		String log = wfMgr.getWorkflowLog(m_parent.getWorkflowModelId(), m_podName);
		System.out.println(log);
	}
}