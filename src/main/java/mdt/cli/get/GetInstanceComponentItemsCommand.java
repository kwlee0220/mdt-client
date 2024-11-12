package mdt.cli.get;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.Funcs;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.service.SubmodelService;
import mdt.model.sm.info.ComponentItem;
import mdt.model.sm.info.DefaultTwinComposition;
import mdt.model.sm.info.InformationModel;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "components",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get composition items of an instance"
)
public class GetInstanceComponentItemsCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceComponentItemsCommand.class);
	
	@ParentCommand GetMDTInstanceCommand m_parent;

	@Option(names={"--type"}, paramLabel="dependency-type",
					description="CompositionDependency type")
	private String m_type;

	public static final void main(String... args) throws Exception {
		main(new GetInstanceComponentItemsCommand(), args);
	}
	
	public GetInstanceComponentItemsCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		HttpMDTInstanceClient instance = client.getInstance(m_parent.getInstanceId());
		
		SubmodelService infoSvc = Funcs.getFirstOrNull(instance.getAllSubmodelServiceBySemanticId(InformationModel.SEMANTIC_ID));
		if ( infoSvc == null ) {
			System.out.println("InformationModel is not found");
			System.exit(0);
		}
		
		SubmodelElement sme = infoSvc.getSubmodelElementByPath("TwinComposition");
		if ( sme == null ) {
			System.err.println("Cannot find TwinComposition");
			System.exit(-1);
		}
		if ( sme instanceof SubmodelElementCollection smc ) {
			DefaultTwinComposition tc = new DefaultTwinComposition();
			tc.updateFromAasModel(sme);
			
			for ( ComponentItem item: tc.getComponentItems() ) {
				System.out.printf("%s (%s)%n", item.getID(), item.getReference());
			}
		}
		else {
			System.err.println("Unexpected ComponentItems type: " + sme);
			System.exit(-1);
		}
	}
}
