package mdt.cli.workflow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.io.FileUtils;

import mdt.cli.MDTCommand;
import mdt.client.workflow.HttpWorkflowManagerProxy;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.workflow.model.WorkflowDescriptor;

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
	description = "Get an MDT Workflow Descriptor.",
	subcommands = {
		GetArgoWorkflowScriptCommand.class,
	})
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
	
	public String getWorkflowDescriptorId() {
		return m_wfId;
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpWorkflowManagerProxy client = (HttpWorkflowManagerProxy)manager.getWorkflowManager();
		
		WorkflowDescriptor wfDesc = client.getWorkflowDescriptor(m_wfId);
		
		if ( m_outFile != null ) {
			m_outFile = m_outFile.getAbsoluteFile();
			if ( m_outFile != null && m_outFile.getParentFile() != null ) {
				FileUtils.createDirectories(m_outFile.getParentFile());
			}
		}
		
		try ( OutputStream os = (m_outFile != null) ? new FileOutputStream(m_outFile) : System.out;
				BufferedOutputStream bos = new BufferedOutputStream(os) ) {
			MDTModelSerDe.getJsonMapper()
					.writerWithDefaultPrettyPrinter()
					.writeValue(os, wfDesc);
		}
	}
}
