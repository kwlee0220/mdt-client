package mdt.cli.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.io.FileUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowDescriptorService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "argo",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an Argo Workflow Descriptor."
)
public class GetArgoWorkflowScriptCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetArgoWorkflowScriptCommand.class);
	
	@ParentCommand GetWorkflowDescriptorCommand m_parent;

	@Option(names={"--client-image"}, paramLabel="name", required=true, description="MDTClient docker image name")
	private String m_clientImage;

	@Option(names={"--output", "-o"}, paramLabel="path", required=false, description="output file")
	private File m_outFile;

	public static final void main(String... args) throws Exception {
		main(new GetArgoWorkflowScriptCommand(), args);
	}
	
	public GetArgoWorkflowScriptCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowDescriptorService svc = ((HttpMDTManagerClient)mdt).createClient(WorkflowDescriptorService.class);
		
		String wfId = m_parent.getWorkflowDescriptorId();
		String scriptYaml = svc.getArgoWorkflowDescriptor(wfId, m_clientImage);
		
		if ( m_outFile != null && m_outFile.getParentFile() != null ) {
			FileUtils.createDirectory(m_outFile.getParentFile());
		}
		try ( OutputStream os = (m_outFile != null) ? new FileOutputStream(m_outFile) : System.out;
				PrintWriter pw = new PrintWriter(os) ) {
			pw.print(scriptYaml);
		}
	}
}
