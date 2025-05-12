package mdt.cli.get;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.DescriptorUtils;
import mdt.model.MDTManager;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReferences;

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
public class GetSubmodelCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelCommand.class);

	@Parameters(index="0", arity="0..1", paramLabel="ref",
				description="SubmodelReference (<twin-id>/<submodel_idshort>) to show")
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
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		if ( m_submodelRefString == null && m_submodelId == null ) {
			System.err.println("Either SubmodelReference ('ref') or SubmodelId ('id') "
								+ "should be provided");
			System.exit(-1);
		}
		
		String submodelId = m_submodelId;
		if ( m_submodelRefString != null ) {
			try {
				DefaultSubmodelReference ref = ElementReferences.parseSubmodelReference(m_submodelRefString);
				ref.activate(manager);
				
				Submodel submodel = ref.get().getSubmodel();
				submodelId = submodel.getId();
			}
			catch ( ResourceNotFoundException e ) {
				System.err.printf("Unknown SubmodelReference: %s", m_submodelRefString);
				System.exit(-1);
			}
			catch ( Throwable e ) { }
		}
		
		SubmodelDescriptor smDesc = mdt.getSubmodelRegistry().getSubmodelDescriptorById(submodelId);
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsTable(smDesc);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(smDesc);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		main(new GetSubmodelCommand(), args);
	}
	
	private void displayAsJson(SubmodelDescriptor smDesc)
		throws SerializationException {
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(smDesc);
		System.out.println(jsonStr);
	}
	
	private void displayAsTable(SubmodelDescriptor smDesc) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + smDesc.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(smDesc.getIdShort()) + " ");
		
		table.addCell(" SEMANTIC_ID ");
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(smDesc.getSemanticId());
		if ( semanticId != null ) {
			String msg = String.format(" (%s) %s", SubmodelUtils.getShortSubmodelSemanticId(semanticId), semanticId);
			table.addCell(msg);
		}
		
		String endpoint = DescriptorUtils.getEndpointString(smDesc.getEndpoints());
		table.addCell(" ENDPOINT "); table.addCell(" " + getOrEmpty(endpoint));
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
