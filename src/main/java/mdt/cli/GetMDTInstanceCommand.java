package mdt.cli;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.IdPair;
import mdt.model.instance.MDTInstance;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "instance", description = "get MDTInstance runtime information.")
public class GetMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetMDTInstanceCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="MDTInstance id to show")
	private String m_instanceId;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'table' or 'json')")
	private String m_output = "table";
	
	public GetMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mdtClient = this.createMDTInstanceManager(configs);
		HttpMDTInstanceClient instance = mdtClient.getInstance(m_instanceId);
		
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsTable(instance);
		}
		else if ( m_output.startsWith("json") ) {
			displayAsJson(instance);
		}
		else {
			System.err.println("Unsupported output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		GetMDTInstanceCommand cmd = new GetMDTInstanceCommand();

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
	
	private void displayAsJson(MDTInstance instance) throws SerializationException {
		AssetAdministrationShellDescriptor desc = instance.getAASDescriptor();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(desc);
		System.out.println(jsonStr);
	}
	
	private void displayAsTable(HttpMDTInstanceClient instance) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" INSTANCE "); table.addCell(" " + instance.getId());
		
		AssetAdministrationShellDescriptor aasDesc = instance.getAASDescriptor();
		table.addCell(" AAS_ID "); table.addCell(" " + IdPair.of(aasDesc.getId(), aasDesc.getIdShort()) + " ");
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(aasDesc.getIdShort()) + " ");
		table.addCell(" GLOBAL_ASSET_ID "); table.addCell(" " + getOrEmpty(aasDesc.getGlobalAssetId()) + " ");
		table.addCell(" ASSET_TYPE "); table.addCell(" " + getOrEmpty(aasDesc.getAssetType()) + " ");
		table.addCell(" ASSET_KIND "); table.addCell(" " + getOrEmpty(aasDesc.getAssetKind()) + " ");
		FStream.from(instance.getInstanceSubmodelDescriptors())
				.map(smd -> IdPair.of(smd.getId(), smd.getIdShort()))
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL[%02d] ", tup.index()));
					table.addCell(" " + tup.value() + " ");
				});
		table.addCell(" STATUS "); table.addCell(" " + instance.getStatus().toString());
		String epStr = instance.getEndpoint();
		epStr = (epStr != null) ? instance.getEndpoint() : "";
		table.addCell(" ENDPOINT "); table.addCell(" " + epStr);
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
