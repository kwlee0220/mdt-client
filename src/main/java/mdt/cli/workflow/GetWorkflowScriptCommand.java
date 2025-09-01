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
	name = "script",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an MDT Workflow script."
)
public class GetWorkflowScriptCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetWorkflowScriptCommand.class);
	
	@ParentCommand
	private GetWorkflowModelCommand m_parent;
	
	@Option(names={"--mdt-endpoint"}, paramLabel="url", required=false,
			description="endpoint URL to the MDTManager")
	private String m_mdtEndpoint = null;
	
	@Option(names={"--mdt-client-docker"}, paramLabel="image-id", required=false,
			description="docker image of the MDTClient")
	private String m_clientDockerImage = null;

	public static final void main(String... args) throws Exception {
		main(new GetWorkflowScriptCommand(), args);
	}
	
	public GetWorkflowScriptCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		String script = wfMgr.getWorkflowScript(m_parent.getWorkflowModelId(), m_mdtEndpoint, m_clientDockerImage);
		System.out.println(script);
	}
}
