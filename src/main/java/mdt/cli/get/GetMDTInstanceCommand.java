package mdt.cli.get;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.IdPair;
import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.SubmodelUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "instance",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an MDTInstance information.",
	subcommands = {
		GetInstanceComponentItemsCommand.class,
		GetInstanceCompositionDependenciesCommand.class,
		GetInstanceLogCommand.class,
	}
)
public class GetMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetMDTInstanceCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="MDTInstance id to show")
	private String m_instanceId;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'table', 'json', or 'env')")
	private String m_output = "table";

	public static final void main(String... args) throws Exception {
		main(new GetMDTInstanceCommand(), args);
	}
	
	public GetMDTInstanceCommand() {
		setLogger(s_logger);
	}
	
	String getInstanceId() {
		return m_instanceId;
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		HttpMDTInstanceClient instance = client.getInstance(m_instanceId);
		
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsTable(instance);
		}
		else if ( m_output.startsWith("json") ) {
			displayAsJson(instance);
		}
		else if ( m_output.equalsIgnoreCase("env") ) {
			displayEnvironment(instance);
		}
		else {
			System.err.println("Unsupported output: " + m_output);
			System.exit(-1);
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
		FStream.from(instance.getAllInstanceSubmodelDescriptors())
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL[%02d] ", tup.index()));
					table.addCell(" " + toDisplayName(tup.value()) + " ");
				});
		table.addCell(" STATUS "); table.addCell(" " + instance.getStatus().toString());
		String epStr = instance.getEndpoint();
		epStr = (epStr != null) ? instance.getEndpoint() : "";
		table.addCell(" ENDPOINT "); table.addCell(" " + epStr);
		
		System.out.println(table.render());
	}
	
	private void displayEnvironment(HttpMDTInstanceClient instance) throws SerializationException {	
		AssetAdministrationShell aas = instance.getAssetAdministrationShellService()
												.getAssetAdministrationShell();
		List<Submodel> submodels = FStream.from(instance.getAllSubmodelServices())
											.map(SubmodelService::getSubmodel)
											.toList();
		Environment env = new DefaultEnvironment.Builder()
								.assetAdministrationShells(aas)
								.submodels(submodels)
								.build();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(env);
		System.out.println(jsonStr);
	}
	
	private static String toDisplayName(InstanceSubmodelDescriptor ismdesc) {
		return String.format("(%s) %s (%s)", SubmodelUtils.getShortSubmodelSemanticId(ismdesc.getSemanticId()),
							ismdesc.getId(), ismdesc.getIdShort());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
