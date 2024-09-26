package mdt.cli.workflow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.MDTCommand;
import mdt.client.workflow.HttpWorkflowManagerClient;
import mdt.model.AASUtils;
import mdt.model.MDTManager;
import mdt.model.workflow.descriptor.WorkflowDescriptor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "get",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an MDT Workflow Descriptor."
)
public class GetWorkflowDescriptorCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetWorkflowDescriptorCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="Workflow id to get")
	private String m_wfId;

	@Option(names={"--output", "-o"}, paramLabel="path", required=false, description="output file")
	private File m_outFile;

	public static final void main(String... args) throws Exception {
		main(new GetWorkflowDescriptorCommand(), args);
	}
	
	public GetWorkflowDescriptorCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpWorkflowManagerClient client = (HttpWorkflowManagerClient)manager.getWorkflowManager();
		
		WorkflowDescriptor wfDesc = client.getWorkflowDescriptor(m_wfId);
		if ( m_outFile != null ) {
			m_outFile.getParentFile().mkdirs();
		}
		
		try ( OutputStream os = (m_outFile != null) ? new FileOutputStream(m_outFile) : System.out;
				BufferedOutputStream bos = new BufferedOutputStream(os) ) {
			AASUtils.getJsonMapper()
					.writerWithDefaultPrettyPrinter()
					.writeValue(os, wfDesc);
		}
	}
}
