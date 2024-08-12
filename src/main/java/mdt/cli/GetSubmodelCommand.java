package mdt.cli;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "submodel", description = "get Submodel information.")
public class GetSubmodelCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelCommand.class);

	@Parameters(index="0", arity="1", paramLabel="id", description="Submodel id to show")
	private String m_submodelId = null;
	
	@Option(names={"--mdt"}, paramLabel="id", description="MDTInstance id to show")
	private String m_mdtId = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table or json)")
	private String m_output = "table";
	
	public GetSubmodelCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient client = this.createMDTInstanceManager(configs);
		
		SubmodelService submodelSvc = null;
		try {
			HttpMDTInstanceClient inst = (HttpMDTInstanceClient)client.getInstanceBySubmodelId(m_submodelId);
			submodelSvc = inst.getSubmodelServiceById(m_submodelId);
		}
		catch ( ResourceNotFoundException expected ) {
			if ( m_mdtId == null ) {
				System.err.printf("Unknown Submodel id: %s", m_submodelId);
				System.exit(-1);
			}
			HttpMDTInstanceClient inst = client.getInstance(m_mdtId);
			submodelSvc = inst.getSubmodelServiceByIdShort(m_submodelId);
		}
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsSimple(submodelSvc);
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
		GetSubmodelCommand cmd = new GetSubmodelCommand();

		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(100);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				cmd.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
	
	private void displayAsJson(SubmodelService submodelSvc)
		throws SerializationException {
		Submodel submodel = submodelSvc.getSubmodel();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(submodel);
		System.out.println(jsonStr);
	}
	
	private void displayAsSimple(SubmodelService submodelSvc) {
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
		
//		String epStr = RegistryModelConverter.getEndpointString(submodelSvc.getEndpoint()); 
//		table.addCell(" ENDPOINT "); table.addCell(" " + epStr);
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
