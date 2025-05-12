package mdt.cli.get;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.info.CompositionItem;
import mdt.model.sm.info.DefaultCompopentItems;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.value.ElementValues;
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
public class GetInstanceCompositionItemsCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceCompositionItemsCommand.class);
	
	@ParentCommand GetInstanceCommand m_parent;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'simple' or 'json')")
	private String m_output = "simple";

	public static final void main(String... args) throws Exception {
		main(new GetInstanceCompositionItemsCommand(), args);
	}
	
	public GetInstanceCompositionItemsCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManager client = (HttpMDTInstanceManager)manager.getInstanceManager();
		HttpMDTInstance instance = client.getInstance(m_parent.getInstanceId());

		SubmodelService infoSvc = FStream.from(instance.getSubmodelServiceAllBySemanticId(InformationModel.SEMANTIC_ID))
								        .findFirst()
										.getOrThrow(() -> new ResourceNotFoundException("InformationModelService"));
		SubmodelElement sme = infoSvc.getSubmodelElementByPath("TwinComposition.CompositionItems");
		if ( sme instanceof SubmodelElementList ) {
			switch ( m_output ) {
				case "simple":
					DefaultCompopentItems items = new DefaultCompopentItems();
					items.updateFromAasModel(sme);
					for ( CompositionItem item: items.getElementAll() ) {
						System.out.printf("%s (%s)%n", item.getID(), item.getReference());
					}
					break;
				case "json":
					System.out.println(MDTModelSerDe.toJsonString(ElementValues.getValue(sme)));
					break;
				default:
					System.err.println("Unexpected output type: " + m_output);
					System.exit(-1);
			}
		}
		else {
			System.err.println("Unexpected CompositionItems type: " + sme);
			System.exit(-1);
		}
	}
}
