package mdt.cli.workflow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.workflow.HttpWorkflowManagerProxy;
import mdt.model.MDTManager;
import mdt.workflow.model.WorkflowDescriptor;
import mdt.workflow.model.argo.ArgoWorkflowDescriptor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "convert",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Convert an MDT Workflow Descriptor into Argo workflow descriptor."
)
public class ConvertWorkflowDescriptorCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ConvertWorkflowDescriptorCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="Workflow id to get")
	private String m_wfId;

	@Option(names={"--client-image"}, paramLabel="name", required=true, description="MDTClient docker image name")
	private String m_clientImage;

	@Option(names={"--output", "-o"}, paramLabel="path", required=false, description="output file")
	private File m_outFile;
	
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();

	public static final void main(String... args) throws Exception {
		main(new ConvertWorkflowDescriptorCommand(), args);
	}
	
	public ConvertWorkflowDescriptorCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpWorkflowManagerProxy client = (HttpWorkflowManagerProxy)manager.getWorkflowManager();
		
		String mdtInstMgrEndpoint = ((HttpMDTInstanceManagerClient)manager.getInstanceManager()).getEndpoint();
		
		WorkflowDescriptor wfDesc = client.getWorkflowDescriptor(m_wfId);
		
		Map<String,String> arguments = Maps.newHashMap();
		for ( int i =0; i < m_unmatcheds.size(); ++i ) {
			if ( m_unmatcheds.get(i).startsWith("--arg.") ) {
				String argName =  m_unmatcheds.get(i).substring(6);
				String argValue = m_unmatcheds.get(i+1);
				arguments.put(argName, argValue);
			}
		}
		
		ArgoWorkflowDescriptor argoWfDesc = new ArgoWorkflowDescriptor(wfDesc, mdtInstMgrEndpoint, m_clientImage);
		
		if ( m_outFile != null ) {
			m_outFile.getParentFile().mkdirs();
		}
		
		try ( OutputStream os = (m_outFile != null) ? new FileOutputStream(m_outFile) : System.out;
				BufferedOutputStream bos = new BufferedOutputStream(os) ) {
			YAMLFactory yamlFact = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
													.enable(Feature.MINIMIZE_QUOTES);
			JsonMapper.builder(yamlFact).build()
					.writerWithDefaultPrettyPrinter()
					.writeValue(os, argoWfDesc);
		}
	}
}
