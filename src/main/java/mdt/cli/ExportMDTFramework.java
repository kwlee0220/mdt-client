package mdt.cli;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.DefaultMDTInstanceInfo;
import mdt.model.instance.MDTInstanceInfo;
import mdt.model.instance.MDTInstanceManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "export",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Export the current states of all MDT framework instances."
)
public class ExportMDTFramework extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ExportMDTFramework.class);
	
	@Option(names={"--output", "-o"}, paramLabel="path", required=false, description="output file")
	private File m_outFile = null;

	public static final void main(String... args) throws Exception {
		main(new ExportMDTFramework(), args);
	}

	public ExportMDTFramework() {
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		List<? extends MDTInstanceInfo> mdtInfoList = FStream.from(manager.getInstanceAll())
															.map(inst -> DefaultMDTInstanceInfo.builder(inst).build())
															.toList();
		if ( m_outFile != null ) {
			String json = MDTModelSerDe.toJsonString(mdtInfoList);
			IOUtils.toFile(json, m_outFile);
		}
		else {
			System.out.println(MDTModelSerDe.toJsonString(mdtInfoList));
		}
	}
}
