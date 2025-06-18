package mdt.cli.get;

import java.io.File;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.io.IOUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;
import mdt.model.SubmodelService;
import mdt.model.sm.AASFile;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "file",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get a file from the File SubmodelElement."
)
public class GetFile extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetFile.class);

	@Parameters(index="0", arity="1", paramLabel="element-ref",
				description="Target File SubmodelElement reference")
	private String m_elmRef = null;
	
	@Option(names={"--file", "-f"}, paramLabel="path", description="file path to save output")
	private File m_outputFile;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new GetFile(), args);
	}
	
	public GetFile() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		ElementReference smeRef = ElementReferences.parseExpr(m_elmRef);
		if ( !(smeRef instanceof MDTElementReference) ) {
			throw new IllegalArgumentException("Target element reference must be an instance of MDTElementReference:"
												+ smeRef);
		}
		
		MDTElementReference iref = (MDTElementReference)smeRef;
		iref.activate(manager);
		
		SubmodelElement sme = smeRef.read();
		if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File file ) {
			SubmodelService svc = iref.getSubmodelService();
			AASFile aasFile = svc.getFileByPath(iref.getIdShortPathString());
			
			if ( m_outputFile == null ) {
				m_outputFile = new File(aasFile.getPath());
			}
			IOUtils.toFile(aasFile.getContent(), m_outputFile);
			if ( m_verbose ) {
				System.out.printf("File downloaded to: %s%n", m_outputFile.getAbsolutePath());
			}
		}
		else {
			System.out.printf("The specified element is not a File type: %s%n", smeRef);
		}
	}
}
