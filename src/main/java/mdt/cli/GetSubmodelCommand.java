package mdt.cli;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.DescriptorUtils;
import mdt.model.MDTManager;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.SubmodelReference;
import mdt.model.service.SubmodelService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "submodel",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get Submodel information."
)
public class GetSubmodelCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelCommand.class);

	@Parameters(index="0", arity="0..1", paramLabel="ref", description="SubmodelReference to show")
	private String m_submodelRefString = null;
	
	@Option(names={"--id"}, paramLabel="id", description="Submodel id to show")
	private String m_submodelId = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table or json)")
	private String m_output = "table";
	
	public GetSubmodelCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		SubmodelService submodelSvc = null;
		if ( m_submodelRefString != null ) {
			try {
				submodelSvc = SubmodelReference.parseString(client, m_submodelRefString).get();
			}
			catch ( ResourceNotFoundException e ) {
				System.err.printf("Unknown SubmodelReference: %s", m_submodelRefString);
				System.exit(-1);
			}
		}
		else if ( m_submodelId != null ) {
			try {
				MDTInstance inst = client.getInstanceBySubmodelId(m_submodelId);
				submodelSvc = inst.getSubmodelServiceById(m_submodelId);
			}
			catch ( Exception e ) {
				System.err.printf("Unknown Submodel id: %s", m_submodelId);
				System.exit(-1);
			}
		}
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsSimple(manager, submodelSvc);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(submodelSvc);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		main(new GetSubmodelCommand(), args);
	}
	
	private void displayAsJson(SubmodelService submodelSvc)
		throws SerializationException {
		Submodel submodel = submodelSvc.getSubmodel();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(submodel);
		System.out.println(jsonStr);
	}
	
	private void displayAsSimple(MDTManager manager, SubmodelService submodelSvc) {
		Table table = new Table(2);
		Submodel submodel = submodelSvc.getSubmodel();

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + submodel.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(submodel.getIdShort()) + " ");
		
		table.addCell(" SEMANTIC_ID ");
		Reference semanticId = submodel.getSemanticId();
		if ( semanticId != null ) {
			String id = semanticId.getKeys().get(0).getValue();
			table.addCell(" " + id);
		}

		FStream.from(submodel.getSubmodelElements())
				.map(sme -> sme.getIdShort())
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL_ELEMENT[%02d] ", tup.index()));
					table.addCell(" " + tup.value() + " ");
				});
		
		List<LangStringNameType> names = submodel.getDisplayName();
		if ( names != null ) {
			String displayName = FStream.from(names)
										.map(name -> name.getText())
										.join(". ");
			table.addCell(" DISPLAY_NAME "); table.addCell(" " + displayName);
		}
		else {
			table.addCell(" DISPLAY_NAME "); table.addCell("");
		}
		
		List<LangStringTextType> descs = submodel.getDescription();
		if ( names != null ) {
			String description = FStream.from(descs)
										.map(desc -> desc.getText())
										.join(". ");
			table.addCell(" DESCRIPTION "); table.addCell(" " + description);
		}
		else {
			table.addCell(" DESCRIPTION "); table.addCell("");
		}
		
		SubmodelDescriptor descriptor = manager.getSubmodelRegistry()
												.getSubmodelDescriptorById(submodel.getId());
		String endpoint = DescriptorUtils.getEndpointString(descriptor.getEndpoints());
		table.addCell(" ENDPOINT "); table.addCell(" " + endpoint);
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
